package cronapi.odata.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cronapi.AppConfig;
import cronapi.CronapiSearchable;
import cronapi.QueryManager;
import cronapi.util.ReflectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.*;
import org.apache.olingo.odata2.core.CloneUtils;
import org.apache.olingo.odata2.core.edm.EdmSimpleTypeFacadeImpl;
import org.apache.olingo.odata2.core.edm.EdmString;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmExtension;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmSchemaView;
import org.apache.olingo.odata2.jpa.processor.core.ODataJPAConfig;
import org.apache.olingo.odata2.jpa.processor.core.access.data.VirtualClass;
import org.apache.olingo.odata2.jpa.processor.core.access.model.JPATypeConverter;
import org.apache.olingo.odata2.jpa.processor.core.model.JPAEdmMappingImpl;
import org.eclipse.persistence.internal.expressions.ConstantExpression;
import org.eclipse.persistence.internal.expressions.SubSelectExpression;
import org.eclipse.persistence.internal.jpa.EntityManagerImpl;
import org.eclipse.persistence.internal.jpa.jpql.HermesParser;
import org.eclipse.persistence.internal.queries.ReportItem;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.jpa.jpql.parser.*;
import org.eclipse.persistence.jpa.jpql.utility.iterable.ListIterable;
import org.eclipse.persistence.queries.ReadAllQuery;
import org.eclipse.persistence.queries.ReportQuery;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Pattern;

public class DatasourceExtension implements JPAEdmExtension {

  public static Set<String> GRID_PREFERED_FIELDS;

  public static final String JPQL = "jpql";
  private final EntityManagerImpl em;
  private int order;
  private String jpql;
  private EntityType jpqlEntity;

  static {
    GRID_PREFERED_FIELDS = new HashSet<>();
    GRID_PREFERED_FIELDS.add("(.*?name.*?|.*?nome.*?|.*?title.*?|.*?titulo.*?|.*?firstname.*?|.*?primeironome.*?|.*?description.*?|.*?descricao.*?|.*?jobtitle.*?|^cpf$|^rg$)");
  }


  public DatasourceExtension(ODataJPAContext context, int order) {
    this((EntityManagerImpl) context.getEntityManager(), order);
  }

  public DatasourceExtension(EntityManagerImpl em, int order) {
    this.em = em;
    this.order = order;
  }

  public void jpql(String jpql) {
    this.jpql = jpql;
  }

  public EntityType getJpqlEntity() {
    return this.jpqlEntity;
  }

  @Override
  public void extendWithOperation(JPAEdmSchemaView jpaEdmSchemaView) {
    jpaEdmSchemaView.registerOperations(DatasourceOperations.class, null);
  }

  @Override
  public void extendJPAEdmSchema(JPAEdmSchemaView view) {
    extendJPAEdmSchema(view.getEdmSchema());
  }

  public void extendJPAEdmSchema(Schema edmSchema) {

    if (jpql != null) {
      createJpqlDataSource(edmSchema, JPQL, jpql, null, null);

      for (EntityType type : edmSchema.getEntityTypes()) {
        if (!type.getName().equals(JPQL)) {
          type.setShowMetadata(false);
        } else {
          this.jpqlEntity = type;
        }
      }
      //A

      for (EntityContainer container : edmSchema.getEntityContainers()) {
        for (EntitySet entitySet : container.getEntitySets()) {
          entitySet.setShowMetadata(false);
        }
      }

    } else {

      for (EntitySet set : edmSchema.getEntityContainers().get(0).getEntitySets()) {
        set.setShowMetadata(AppConfig.exposeLocalEntities());
      }

      List<EntityType> localEntities = new LinkedList<>();

      for (EntityType type : edmSchema.getEntityTypes()) {
        localEntities.add(type);
      }

      List<EntitySet> queryDatasource = new LinkedList<>();

      JsonObject queries = QueryManager.getJSON();
      for (Map.Entry<String, JsonElement> entry : queries.entrySet()) {
        JsonObject customObj = entry.getValue().getAsJsonObject();

        LinkedList<CalcField> calcFields = new LinkedList<>();

        if (!QueryManager.isNull(customObj.get("calcFieldsProperties"))) {
          if (!QueryManager.isNull(customObj.get("calcFieldsProperties"))) {
            JsonObject calcObj = customObj.get("calcFieldsProperties").getAsJsonObject();
            for (Map.Entry<String, JsonElement> entryObj : calcObj.entrySet()) {
              CalcField field = new CalcField();
              field.name = entryObj.getKey();
              field.type = entryObj.getValue().getAsJsonObject().get("type").getAsString();
              if (!QueryManager.isNull(customObj.get("calcFields"))) {
                JsonElement value = customObj.get("calcFields").getAsJsonObject().get(field.name);
                if (!QueryManager.isNull(value) && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString() && StringUtils.isNotEmpty(value.getAsString())) {
                  field.expression = value.getAsString();
                }
              }
              calcFields.add(field);
            }
          }
        } else {
          //Suporte ao legado
          if (!QueryManager.isNull(customObj.get("calcFields"))) {
            JsonObject calcObj = customObj.get("calcFields").getAsJsonObject();
            for (Map.Entry<String, JsonElement> entryObj : calcObj.entrySet()) {
              CalcField field = new CalcField();
              field.name = entryObj.getKey();
              calcFields.add(field);
            }
          }
        }

        if (!QueryManager.isNull(customObj.get("entityFullName"))) {
          String clazz = customObj.get("entityFullName").getAsString();
          if (clazz.startsWith(edmSchema.getNamespace() + ".")) {
            try {
              EntitySet set = createDataSource(edmSchema, customObj.get("customId").getAsString(),
                  customObj.get("entitySimpleName").getAsString(), calcFields);
              if (set != null) {
                queryDatasource.add(set);
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        } else {
          if (!QueryManager.isNull(customObj.get("baseEntity"))) {
            String baseEntity = customObj.get("baseEntity").getAsString();
            if (baseEntity.startsWith(edmSchema.getNamespace() + ".")) {
              try {
                EntitySet set = createBlocklyDataSource(edmSchema,
                    customObj.get("customId").getAsString(), customObj, calcFields);
                if (set != null) {
                  queryDatasource.add(set);
                }
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          } else {
            if (order == 0) {
              try {
                EntitySet set = createBlocklyDataSource(edmSchema,
                    customObj.get("customId").getAsString(), customObj, calcFields);
                if (set != null) {
                  queryDatasource.add(set);
                }
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          }
        }

      }

      if (!AppConfig.exposeLocalEntities()) {
        edmSchema.getEntityContainers().get(0).getAssociationSets().clear();

        for (EntityType type : edmSchema.getEntityTypes()) {
          if (type.getNavigationProperties() != null) {
            type.getNavigationProperties().clear();
          }
        }
      }

      for (EntityType type : localEntities) {
        JPAEdmMappingImpl mapping = (JPAEdmMappingImpl) type.getMapping();
        if (mapping != null && !mapping.isVirtualAccess()) {
          addDisplayFields(edmSchema, type);
        }
      }

      for (EntityType type : edmSchema.getEntityTypes()) {
        type.setShowMetadata(AppConfig.exposeMetadada());
      }

      for (EntityContainer container : edmSchema.getEntityContainers()) {
        for (EntitySet set : container.getEntitySets()) {
          set.setShowMetadata(AppConfig.exposeMetadada());
        }
      }
    }
  }

  private static boolean isEdmSimpleTypeKind(Class fieldClass) {
    try {
      JPATypeConverter.convertToEdmSimpleType(fieldClass, null);
      return false;
    } catch (ODataJPAModelException e) {
      return true;
    }
  }

  private static EdmSimpleTypeKind toEdmSimpleTypeKind(Class fieldClass) {
    try {
      return JPATypeConverter.convertToEdmSimpleType(fieldClass, null);
    } catch (ODataJPAModelException e) {
      return EdmSimpleTypeKind.Auto;
    }
  }

  private static EdmSimpleTypeKind toEdmSimpleTypeKind(String fieldClass) {
    return Enum.valueOf(EdmSimpleTypeKind.class, fieldClass);
  }

  private EntityType findEntityType(Schema edmSchema, String entity) {
    for (EntityType type : edmSchema.getEntityTypes()) {
      if (type.getName().equals(entity)) {
        return type;
      }
    }

    return null;
  }

  private PropertyRef findKey(EntityType entityType, String name) {
    for (PropertyRef item : entityType.getKey().getKeys()) {
      if (item.getName().equals(name)) {
        return item;
      }
    }

    return null;
  }

  private boolean isOriginalKey(EntityType entityType, String name) {
    for (Property item : entityType.getProperties()) {
      if (item.getName().equals(name) && item.isOriginalId()) {
        return true;
      }
    }

    return false;
  }

  private List<Property> findOriginalKeys(EntityType entityType) {
    List<Property> keys = new LinkedList<>();
    for (PropertyRef item : entityType.getKey().getKeys()) {
      Property property = findProperty(entityType, item.getName());
      if (property != null) {
        if (property.getComposite() != null) {
          keys.addAll(property.getComposite());
        } else {
          keys.add(property);
        }
      }
    }
    return keys;
  }

  private Property findProperty(EntityType entityType, String name) {
    for (Property item : entityType.getProperties()) {
      if (item.getName().equals(name)) {
        return item;
      }
    }

    return null;
  }

  private void addCalcFields(EntityType newType, List<CalcField> addFields, String mainAlias, boolean addAlias) {
    if (addFields != null) {
      for (CalcField field : addFields) {
        SimpleProperty property = new SimpleProperty();
        EdmSimpleTypeKind kind;
        if (field.type != null && !field.type.isEmpty()) {
          kind = EdmSimpleTypeKind.valueOf(field.type);
        } else {
          kind = EdmSimpleTypeKind.Auto;
        }
        property.setType(kind);
        property.setName(field.name);

        JPAEdmMappingImpl mapping = new JPAEdmMappingImpl();
        mapping.setInternalName(field.name);
        if (StringUtils.isNotEmpty(mainAlias) && StringUtils.isNotEmpty(field.expression) && field.expression.startsWith("this.")) {
          if (addAlias) {
            mapping.setVirtualAccess(true);
            mapping.setInternalExpression(mainAlias + "." + field.expression.substring(5));
          } else {
            mapping.setInternalName(field.expression.substring(5));
          }
        } else {
          mapping.setCalculated(true);
          mapping.setVirtualAccess(true);
        }
        mapping.setJPAType(EdmSimpleTypeFacadeImpl.getEdmClassType(kind));

        property.setMapping(mapping);

        newType.getProperties().add(property);
      }
    }
  }

  public static boolean isPreferedDisplayField(String name) {
    for (String pref : GRID_PREFERED_FIELDS) {
      if (name.matches(pref)) {
        return true;
      }
    }

    return false;
  }

  private Property findBestDisplayField(EntityType complexType) {
    if (!ODataJPAConfig.ADD_DISPLAY_FIELDS) {
      return null;
    }
    Class clazz = ((JPAEdmMappingImpl) complexType.getMapping()).getJPAType();
    Field[] fields = clazz.getDeclaredFields();
    for (Field field : fields) {
      try {
        CronapiSearchable searchable = field.getAnnotation(CronapiSearchable.class);
        if (searchable != null) {
          return findProperty(complexType, field.getName());
        }
      } catch (Exception e) {
        //
      }
    }

    Property best = null;
    for (Property p : complexType.getProperties()) {
      JPAEdmMappingImpl mapping = (JPAEdmMappingImpl) p.getMapping();
      PropertyRef key = findKey(complexType, p.getName());
      if (key == null && !p.isOriginalId() && p.getOriginalType() == null && mapping.getJPAType() == String.class && isPreferedDisplayField(p.getName())) {
        best = p;
        break;
      }
    }

    if (best == null) {
      for (Property p : complexType.getProperties()) {
        JPAEdmMappingImpl mapping = (JPAEdmMappingImpl) p.getMapping();
        PropertyRef key = findKey(complexType, p.getName());
        if (key == null && !p.isOriginalId() && p.getOriginalType() == null && mapping.getJPAType() == String.class) {
          best = p;
          break;
        }
      }
    }

    if (best == null) {
      for (Property p : complexType.getProperties()) {
        JPAEdmMappingImpl mapping = (JPAEdmMappingImpl) p.getMapping();
        PropertyRef key = findKey(complexType, p.getName());
        boolean isComplex = isEdmSimpleTypeKind(mapping.getJPAType());
        if (key == null && !p.isOriginalId() && p.getOriginalType() == null && !isComplex) {
          best = p;
          break;
        }
      }
    }

    return best;
  }

  private void addDisplayFields(Schema edmSchema, EntityType type) {
    if (!ODataJPAConfig.ADD_DISPLAY_FIELDS) {
      return;
    }
    List<Property> properties = new LinkedList<>();
    properties.addAll(type.getProperties());

    for (Property p : properties) {

      JPAEdmMappingImpl mapping = (JPAEdmMappingImpl) p.getMapping();
      EntityType complexType = null;
      if (p.getOriginalType() != null) {
        complexType = findEntityType(edmSchema, p.getOriginalType().getSimpleName());
      }
      if (complexType != null) {
        SimpleProperty best = (SimpleProperty) findBestDisplayField(complexType);
        if (best != null) {
          SimpleProperty newProp = (SimpleProperty) CloneUtils.getClone(p);
          newProp.setComposite(null);
          newProp.setName(newProp.getName() + "_" + best.getName());
          newProp.setType(best.getType());
          ((Facets) newProp.getFacets()).setNullable(true);

          JPAEdmMappingImpl newMapping = (JPAEdmMappingImpl) CloneUtils.getClone(p.getMapping());
          if (newMapping.getInternalName().contains(".")) {
            newMapping.setInternalName(
                newMapping.getInternalName().substring(0, newMapping.getInternalName().lastIndexOf("."))
                    + "." + best.getName());
          } else {
            newMapping.setInternalName(newMapping.getInternalName() + "." + best.getName());
          }
          newProp.setMapping(newMapping);

          int total = 0;
          String name = newProp.getName();
          for (Property prop : type.getProperties()) {
            if (prop.getName().equals(name) || prop.getName().startsWith(name + "_")) {
              total++;
            }
          }

          if (total > 0) {
            newProp.setName(name + "_" + total);
          }

          type.getProperties().add(newProp);
        }

      }
    }
  }

  public static Pattern PLAIN_EXPRESSION = Pattern.compile("^[a-zA-Z0-9_.-]*$");

  private SimpleProperty addProperty(String alias, EntityType mainType, Schema edmSchema,
                                     Class type, String orgName, String internalName, String expression, List<Property> properties,
                                     List<PropertyRef> propertyRefList, String complexPath, int complexIndex, String mainAlias) {

    boolean isComplex = isEdmSimpleTypeKind(type);
    EntityType complexType = null;
    if (isComplex) {
      complexType = findEntityType(edmSchema, type.getSimpleName());
    }
    if (complexType != null) {
      if (orgName.equals(mainAlias) && alias == null) {
        for (Property property : complexType.getProperties()) {
          if (property.getName().equals(ODataJPAConfig.COMPOSITE_KEY_NAME)) {
            continue;
          }
          Property newProperty = CloneUtils.getClone(property);
          properties.add(newProperty);
          newProperty.setName(newProperty.getName());
          newProperty.setMapping(CloneUtils.getClone(property.getMapping()));
          JPAEdmMappingImpl mapping = ((JPAEdmMappingImpl) newProperty.getMapping());
          mapping.setVirtualAccess(true);
          mapping.setInternalName(mainAlias + "." + mapping.getInternalName());
          mapping.setInternalExpression(expression + "." + property.getMapping().getInternalName());
          mapping.setComplexIndex(complexIndex);
          newProperty.setForeignKey(true);
          if (property.getOriginalType() != null) {
            if (property.getComposite() != null) {
              newProperty.setComposite(null);
              for (Property c : property.getComposite()) {
                Property newComposite = CloneUtils.getClone(c);
                JPAEdmMappingImpl compositeMapping = ((JPAEdmMappingImpl) newComposite.getMapping());
                newComposite.setName(mainAlias + "_" + c.getName());
                compositeMapping.setInternalName(mainAlias + "." + compositeMapping.getInternalName());
                compositeMapping.setInternalExpression(expression + "." + c.getMapping().getInternalName());
                compositeMapping.setComplexIndex(complexIndex);
                compositeMapping.setVirtualAccess(true);
                newComposite.setForeignKey(true);
                newProperty.addComposite(newComposite);
              }
            }

            SimpleProperty best = (SimpleProperty) findBestDisplayField(findEntityType(edmSchema, newProperty.getOriginalType().getSimpleName()));
            if (best != null) {
              if (property.getComposite() != null) {
                addProperty(orgName, mainType, edmSchema,
                    ((JPAEdmMappingImpl) best.getMapping()).getJPAType(),
                    newProperty.getName() + "_" + best.getName(),
                    mapping.getInternalName() + "." + best.getName(), mapping.getInternalExpression() + "." + best.getName(), properties,
                    propertyRefList, expression, complexIndex, mainAlias);
              } else {
                addProperty(orgName, mainType, edmSchema,
                    ((JPAEdmMappingImpl) best.getMapping()).getJPAType(),
                    newProperty.getName() + "_" + best.getName(),
                    mapping.getInternalName().substring(0, mapping.getInternalName().lastIndexOf(".")) + "." + best.getName(), mapping.getInternalExpression().substring(0, mapping.getInternalName().lastIndexOf(".")) + "." + best.getName(), properties,
                    propertyRefList, expression, complexIndex, mainAlias);
              }
            }
          }
        }
        return null;
      }

      String internalExpression = expression.substring(expression.indexOf(".") + 1);
      List<Property> keys = findOriginalKeys(complexType);
      String prefix = alias != null ? alias : internalExpression.replace(".", "_");
      SimpleProperty added = addProperty(orgName, mainType, edmSchema,
          String.class,
          prefix,
          "[name]." + orgName, expression + "." + orgName, properties,
          propertyRefList, expression, complexIndex, mainAlias);
      for (Property key : keys) {
        Property newKey = CloneUtils.getClone(key);
        newKey.setName(added.getName() + "_" + newKey.getName());
        newKey.setMapping(CloneUtils.getClone(key.getMapping()));
        JPAEdmMappingImpl mapping = ((JPAEdmMappingImpl) newKey.getMapping());
        mapping.setVirtualAccess(true);
        mapping.setInternalName(added.getName() + "." + mapping.getInternalName());
        mapping.setInternalExpression(expression + "." + key.getMapping().getInternalName());
        newKey.setForeignKey(true);
        added.addComposite(newKey);
        if (ODataJPAConfig.EXPAND_COMPOSITE_KEYS) {
          properties.add(newKey);
        }
      }

      SimpleProperty best = (SimpleProperty) findBestDisplayField(complexType);
      if (best != null) {
        addProperty(orgName, mainType, edmSchema,
            ((JPAEdmMappingImpl) best.getMapping()).getJPAType(),
            added.getName() + "_" + best.getName(),
            added.getName() + "." + best.getName(), expression + "." + best.getName(), properties,
            propertyRefList, expression, complexIndex, mainAlias);
      }
    } else {

      int count = StringUtils.countMatches(expression, ".");
      boolean useExpression = false;

      if (alias == null && count > 1) {
        useExpression = true;
      }

      SimpleProperty property = new SimpleProperty();

      property.setType(toEdmSimpleTypeKind(type));

      boolean plainExpression = PLAIN_EXPRESSION.matcher(expression).matches();

      if (useExpression) {
        if (plainExpression) {
          orgName = expression.substring(expression.indexOf(".") + 1).replace(".", "_");
        }
        internalName = orgName;
      }

      property.setName(orgName);
      int total = 0;
      String name = orgName;
      for (Property prop : properties) {
        if (prop.getName().equals(name)) {
          total++;
          name = property.getName() + "_" + total;
        }
      }

      if (total > 0) {
        property.setName(name);
      }

      JPAEdmMappingImpl mapping = new JPAEdmMappingImpl();
      mapping.setInternalExpression(expression);
      mapping.setInternalName(internalName.replace("[name]", property.getName()));
      mapping.setJPAType(type);
      mapping.setVirtualAccess(true);
      if (complexPath != null) {
        String path = complexPath.substring(complexPath.indexOf(".") + 1);
        mapping.setComplexPath(path);
      }
      mapping.setComplexIndex(complexIndex);
      if (plainExpression && count > 1) {
        mapping.setIsPath(true);
        String path = expression.substring(expression.indexOf(".") + 1);
        path = path.substring(path.indexOf(".") + 1);
        mapping.setPath(path);
      }

      property.setMapping(mapping);

      properties.add(property);


      return property;
    }

    return null;
  }

  private EntitySet createBlocklyDataSource(Schema edmSchema, String id, JsonObject entity,
                                            List<CalcField> addFields) {
    if (!QueryManager.isNull(entity.get("baseEntity"))) {
      String baseEntity = entity.get("baseEntity").getAsString();
      if (baseEntity.contains(".")) {
        baseEntity = baseEntity.substring(baseEntity.lastIndexOf(".") + 1);
      }
      return createEntityDataSource(edmSchema, id, baseEntity, addFields);
    } else {

      String edmNamespace = edmSchema.getNamespace();

      EntitySet set = new EntitySet();
      set.setName(id);
      set.setEntityType(new FullQualifiedName(edmNamespace, id));

      edmSchema.getEntityContainers().get(0).getEntitySets().add(set);

      Key key = new Key();
      List<PropertyRef> propertyRefList = new ArrayList<>();
      key.setKeys(propertyRefList);

      List<Property> properties = new ArrayList<>();
      List<Property> keys = new LinkedList<>();
      boolean keysSet = false;

      JsonElement defaultValuesPropertiesElement = entity.get("defaultValuesProperties");
      if (defaultValuesPropertiesElement != null) {
        JsonObject obj = defaultValuesPropertiesElement.getAsJsonObject();

        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
          JsonObject propObj = entry.getValue().getAsJsonObject();
          SimpleProperty property = new SimpleProperty();
          property.setName(entry.getKey());

          EdmSimpleTypeKind type = EdmSimpleTypeKind.valueOf(propObj.get("type").getAsString());
          property.setType(type);

          JPAEdmMappingImpl mapping = new JPAEdmMappingImpl();
          mapping.setJPAType(String.class);
          mapping.setVirtualAccess(true);
          mapping.setInternalExpression(entry.getKey());
          mapping.setInternalExpression(entry.getKey());

          property.setMapping(mapping);

          properties.add(property);

          if (entry.getValue().getAsJsonObject().get("key").getAsBoolean()) {
            keys.add(property);
            property.setOriginalId(true);
            keysSet = true;
          }

        }
      }

      boolean canEdit = true;
      if (!keysSet) {
        propertyRefList.clear();
        canEdit = false;
        for (Property item : properties) {
          keys.add(item);
        }
      }

      SimpleProperty objectKey = new SimpleProperty();
      objectKey.setName(ODataJPAConfig.COMPOSITE_KEY_NAME);
      objectKey.setType(EdmSimpleTypeKind.String);

      JPAEdmMappingImpl keymapping = new JPAEdmMappingImpl();
      keymapping.setInternalName(ODataJPAConfig.COMPOSITE_KEY_NAME);
      keymapping.setJPAType(EdmString.class);
      keymapping.setVirtualAccess(true);

      objectKey.setMapping(keymapping);

      objectKey.setComposite(keys);

      PropertyRef propertyRef = new PropertyRef();
      propertyRef.setName(ODataJPAConfig.COMPOSITE_KEY_NAME);
      propertyRefList.add(propertyRef);

      properties.add(objectKey);

      EntityType type = new EntityType();

      type.setProperties(properties);
      type.setKey(key);
      type.setName(id);
      JPAEdmMappingImpl mapping = new JPAEdmMappingImpl();
      mapping.setCanEdit(canEdit);
      mapping.setJPAType(VirtualClass.class);

      mapping.setVirtualAccess(true);
      if (canEdit) {
        mapping.setInternalName(id);
      }
      type.setMapping(mapping);

      addCalcFields(type, addFields, null, false);

      edmSchema.getEntityTypes().add(type);

      return set;
    }
  }

  private EntitySet createDataSource(Schema edmSchema, String id, String entity,
                                     List<CalcField> addFields) {
    JsonObject queryJson = QueryManager.getQuery(id);
    String jpql = QueryManager.getJPQL(queryJson, false);
    return createJpqlDataSource(edmSchema, id, jpql, entity, addFields);
  }

  public String expandJPQL(String jpql, Schema edmSchema) {
    AbstractSession session = em.getActiveSessionIfExists();

    HermesParser parser = new HermesParser();

    ReportQuery reportQuery = (ReportQuery) parser.buildQuery(jpql, session);
    reportQuery.prepareInternal(session);

    JPQLExpression jpqlExpression = new JPQLExpression(
        jpql,
        DefaultEclipseLinkJPQLGrammar.instance(),
        true
    );

    ListIterable<Expression> children = ((SelectClause) ((SelectStatement) jpqlExpression
        .getQueryStatement()).getSelectClause()).getSelectExpression().children();
    ListIterator<Expression> expressions = children.iterator();
    SelectStatement selectStatement = ((SelectStatement) jpqlExpression.getQueryStatement());

    List<String> selectResult = new LinkedList<>();

    boolean changed = false;
    String mainAlias = JPQLParserUtil.getMainAlias(jpqlExpression);
    for (ReportItem item : reportQuery.getItems()) {
      String alias = null;
      Expression expression = expressions.next();
      Expression originalExpression = expression;
      if (expression instanceof IdentificationVariable && !(expression
          .getParent() instanceof CollectionExpression)) {
        expression = expression.getParent();
      }

      if (expression instanceof ResultVariable) {
        if (((ResultVariable) expression).getResultVariable() != null) {
          alias = ((ResultVariable) expression).getResultVariable().toActualText();
        }
        expression = ((ResultVariable) expression).getSelectExpression();
      }

      Class type = Object.class;
      if (item.getMapping() != null) {
        type = item.getMapping().getField().getType();
      } else if (item.getDescriptor() != null) {
        type = item.getDescriptor().getJavaClass();
      } else if (item.getResultType() != null) {
        type = item.getResultType();
      } else if (item.getAttributeExpression() instanceof SubSelectExpression) {
        List<ReportItem> subItens = ((SubSelectExpression) item.getAttributeExpression())
            .getSubQuery().getItems();
        if (subItens.size() == 1) {
          type = subItens.get(0).getResultType();
        } else if (subItens.size() > 1) {
          throw new RuntimeException("Error in JPA subquery!");
        }
      } else if ((item.getAttributeExpression() instanceof ConstantExpression) &&
          (((ConstantExpression) item.getAttributeExpression()).getValue() != null)) {
        type = ((ConstantExpression) item.getAttributeExpression()).getValue().getClass();
      }

      boolean isComplex = isEdmSimpleTypeKind(type);
      EntityType complexType = null;
      if (isComplex) {
        complexType = findEntityType(edmSchema, type.getSimpleName());
      }
      if (complexType != null) {
        if (mainAlias.equals(expression.toString())) {
          for (Property prop : complexType.getProperties()) {
            if (prop.getComposite() != null) {
              continue;
            }
            String reportItem = expression + "." + prop.getMapping().getInternalName();
            if (!StringUtils.isEmpty(alias)) {
              reportItem += " as " + alias + "_" + prop.getName();
            }
            selectResult.add(reportItem);
          }
          changed = true;
        } else {
          selectResult.add(originalExpression.toString());
        }
      } else {
        selectResult.add(originalExpression.toString());
      }
    }

    if (changed) {
      ReflectionUtils.setField(selectStatement, "selectClause", null);
      return "SELECT " + StringUtils.join(selectResult, ", ") + " " + jpqlExpression.toString();
    } else {
      return jpql;
    }
  }

  public EntitySet createJpqlDataSource(Schema edmSchema, String id, String jpql, String entity,
                                        List<CalcField> addFields) {

    String edmNamespace = edmSchema.getNamespace();
    AbstractSession session = em.getActiveSessionIfExists();

    HermesParser parser = new HermesParser();
    Object query = parser.buildQuery(jpql, session);

    if (query instanceof ReportQuery) {

      ReportQuery reportQuery = (ReportQuery) parser.buildQuery(jpql, session);
      reportQuery.prepareInternal(session);

      if (reportQuery.getItems().size() == 1
          && reportQuery.getItems().get(0).getDescriptor() != null) {
        entity = reportQuery.getItems().get(0).getDescriptor().getJavaClass().getSimpleName();
        return createEntityDataSource(edmSchema, id, entity, addFields);
      } else {

        String newJpql = jpql;
        boolean changed = false;

        if (!newJpql.equals(jpql)) {
          changed = true;
          jpql = newJpql;
          reportQuery = (ReportQuery) parser.buildQuery(jpql, session);
          reportQuery.prepareInternal(session);
        }

        JPQLExpression jpqlExpression = new JPQLExpression(
            jpql,
            DefaultEclipseLinkJPQLGrammar.instance(),
            true
        );

        String mainEntity = JPQLParserUtil.getMainEntity(jpqlExpression);
        String mainAlias = JPQLParserUtil.getMainAlias(jpqlExpression);

        EntityType mainType = findEntityType(edmSchema, mainEntity);

        EntitySet set = new EntitySet();
        set.setName(id);
        set.setEntityType(new FullQualifiedName(edmNamespace, id));

        edmSchema.getEntityContainers().get(0).getEntitySets().add(set);

        ListIterable<Expression> children = ((SelectClause) ((SelectStatement) jpqlExpression
            .getQueryStatement()).getSelectClause()).getSelectExpression().children();
        ListIterator<Expression> expressions = children.iterator();
        Key key = new Key();
        List<PropertyRef> propertyRefList = new LinkedList<>();
        key.setKeys(propertyRefList);

        List<Property> properties = new LinkedList<>();
        List<Property> keys = new LinkedList<>();
        int index = -1;
        for (ReportItem item : reportQuery.getItems()) {
          index++;
          String alias = null;
          Expression expression = expressions.next();
          if (expression instanceof IdentificationVariable && !(expression
              .getParent() instanceof CollectionExpression)) {
            expression = expression.getParent();
          }

          if (expression instanceof ResultVariable) {
            if (((ResultVariable) expression).getResultVariable() != null) {
              alias = ((ResultVariable) expression).getResultVariable().toActualText();
            }
            expression = ((ResultVariable) expression).getSelectExpression();
          }

          Class type = Object.class;
          if (item.getMapping() != null) {
            type = item.getMapping().getField().getType();
          } else if (item.getDescriptor() != null) {
            type = item.getDescriptor().getJavaClass();
          } else if (item.getResultType() != null) {
            type = item.getResultType();
          } else if (item.getAttributeExpression() instanceof SubSelectExpression) {
            List<ReportItem> subItens = ((SubSelectExpression) item.getAttributeExpression())
                .getSubQuery().getItems();
            if (subItens.size() == 1) {
              type = subItens.get(0).getResultType();
            } else if (subItens.size() > 1) {
              throw new RuntimeException("Error in JPA subquery!");
            }
          } else if ((item.getAttributeExpression() instanceof ConstantExpression) &&
              (((ConstantExpression) item.getAttributeExpression()).getValue() != null)) {
            type = ((ConstantExpression) item.getAttributeExpression()).getValue().getClass();
          }

          String name = item.getName();

          if (name == null || name.isEmpty()) {
            name = "expression";
          }

          addProperty(alias, mainType, edmSchema, type, name, name, expression.toString(), properties, propertyRefList, null, index, mainAlias);
        }

        boolean canEdit = true;
        canEdit = false;
        for (Property item : properties) {
          if (isOriginalKey(mainType, item.getName())) {
            if (item.getComposite() != null) {
              for (Property c : item.getComposite()) {
                keys.add(c);
              }
            } else {
              keys.add(item);
            }
          }
        }

        if (keys.size() == 0) {
          for (Property item : properties) {
            if (item.getComposite() != null) {
              for (Property c : item.getComposite()) {
                keys.add(c);
              }
            } else {
              keys.add(item);
            }
          }
        }

        if (findOriginalKeys(mainType).size() == keys.size()) {
          canEdit = true;
        }

        SimpleProperty objectKey = new SimpleProperty();
        objectKey.setName(ODataJPAConfig.COMPOSITE_KEY_NAME);
        objectKey.setType(EdmSimpleTypeKind.String);

        JPAEdmMappingImpl keymapping = new JPAEdmMappingImpl();
        keymapping.setInternalName(ODataJPAConfig.COMPOSITE_KEY_NAME);
        keymapping.setJPAType(EdmString.class);
        keymapping.setVirtualAccess(true);

        objectKey.setMapping(keymapping);

        objectKey.setComposite(keys);

        PropertyRef propertyRef = new PropertyRef();
        propertyRef.setName(ODataJPAConfig.COMPOSITE_KEY_NAME);
        propertyRefList.add(propertyRef);

        properties.add(objectKey);

        EntityType type = new EntityType();

        type.setProperties(properties);
        type.setKey(key);
        type.setName(id);
        if (changed) {
          type.setJpql(jpql);
        }
        JPAEdmMappingImpl mapping = new JPAEdmMappingImpl();
        mapping.setCanEdit(canEdit);
        mapping.setJPAType(((JPAEdmMappingImpl) mainType.getMapping()).getJPAType());
        mapping.setVirtualAccess(true);
        if (canEdit) {
          mapping.setInternalName(mainEntity);
        }
        type.setMapping(mapping);

        addCalcFields(type, addFields, mainAlias, true);

        edmSchema.getEntityTypes().add(type);

        return set;
      }
    } else {
      if (query instanceof ReadAllQuery) {
        entity = ((ReadAllQuery) query).getExpressionBuilder().getQueryClass().getSimpleName();
      }
      return createEntityDataSource(edmSchema, id, entity, addFields);
    }
  }

  private EntitySet createEntityDataSource(Schema edmSchema, String id, String entity,
                                           List<CalcField> addFields) {

    String edmNamespace = edmSchema.getNamespace();

    for (EntityContainer container : edmSchema.getEntityContainers()) {

      EntitySet foundES = null;

      for (EntitySet entitySet : container.getEntitySets()) {
        if (entitySet.getEntityType().getName().equals(entity)) {
          foundES = entitySet;
          break;
        }
      }

      EntityType oldType = edmSchema.getEntityType(entity);

      EntityType newType = (EntityType) CloneUtils.getClone(oldType);
      newType.setShowMetadata(true);
      newType.setName(id);

      addDisplayFields(edmSchema, newType);

      addCalcFields(newType, addFields, "u", false);

      edmSchema.getEntityTypes().add(newType);

      EntitySet set = new EntitySet();
      set.setName(id);
      set.setEntityType(new FullQualifiedName(edmNamespace, id));
      set.setMapping(foundES.getMapping());
      set.setAnnotationAttributes(foundES.getAnnotationAttributes());
      set.setAnnotationElements(foundES.getAnnotationElements());

      container.getEntitySets().add(set);

      List<AssociationSet> addAssociationSet = new LinkedList<>();

      for (AssociationSet association : container.getAssociationSets()) {
        if (!(association instanceof DSAssociationSet) && association.getEnd1().getRole().equals(entity)) {

          AssociationSet newAssociation = new DSAssociationSet();
          newAssociation.setName(association.getName() + "_" + id);
          newAssociation.setAnnotationAttributes(association.getAnnotationAttributes());
          newAssociation.setAnnotationElements(association.getAnnotationElements());
          newAssociation.setAssociation(association.getAssociation());

          AssociationSetEnd end = new AssociationSetEnd();
          end.setEntitySet(id);
          end.setRole(association.getEnd1().getRole());

          newAssociation.setEnd1(end);
          newAssociation.setEnd2(association.getEnd2());

          addAssociationSet.add(newAssociation);
        }
      }

      for (AssociationSet association : addAssociationSet) {
        container.getAssociationSets().add(association);
      }

      return set;
    }

    return null;
  }

  @Override
  public InputStream getJPAEdmMappingModelStream() {
    return null;
  }

  public static class CalcField {

    String name = null;
    String expression = null;
    String type = null;
  }

  public static class DSAssociationSet extends AssociationSet {

  }
}
