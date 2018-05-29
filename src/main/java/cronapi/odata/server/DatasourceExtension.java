package cronapi.odata.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cronapi.QueryManager;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.*;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmExtension;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmMapping;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmSchemaView;
import org.apache.olingo.odata2.jpa.processor.core.access.model.JPATypeConverter;
import org.apache.olingo.odata2.jpa.processor.core.model.JPAEdmMappingImpl;
import org.eclipse.persistence.internal.jpa.EntityManagerImpl;
import org.eclipse.persistence.internal.jpa.jpql.HermesParser;
import org.eclipse.persistence.internal.queries.ReportItem;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.jpa.jpql.parser.*;
import org.eclipse.persistence.jpa.jpql.utility.iterable.ListIterable;
import org.eclipse.persistence.queries.ReportQuery;

import java.io.InputStream;
import java.util.*;

public class DatasourceExtension implements JPAEdmExtension {

  private final ODataJPAContext context;

  public DatasourceExtension(ODataJPAContext context) {
    this.context = context;
  }

  @Override
  public void extendWithOperation(JPAEdmSchemaView jpaEdmSchemaView) {
    jpaEdmSchemaView.registerOperations(DatasourceOperations.class, null);
  }

  @Override
  public void extendJPAEdmSchema(JPAEdmSchemaView view) {
    Schema edmSchema = view.getEdmSchema();

    for (EntityType entityType : edmSchema.getEntityTypes()) {
      JPAEdmMapping mapping = (JPAEdmMapping) entityType.getMapping();
      if (mapping.getODataJPATombstoneEntityListener() == null) {
        mapping.setODataJPATombstoneEntityListener(QueryExtensionEntityListener.class);
      }
    }

    JsonObject queries = QueryManager.getJSON();
    for (Map.Entry<String, JsonElement> entry : queries.entrySet()) {
      JsonObject customObj = entry.getValue().getAsJsonObject();
      if (!QueryManager.isNull(customObj.get("entityFullName"))) {
        String clazz = customObj.get("entityFullName").getAsString();
        if (clazz.startsWith(edmSchema.getNamespace())) {
          try {
            createDataSource(edmSchema, customObj.get("customId").getAsString(), customObj.get("entitySimpleName").getAsString());
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

  private static CtMethod generateGetter(CtClass declaringClass, String fieldName, Class fieldClass)
      throws CannotCompileException {

    String getterName = "get" + fieldName.substring(0, 1).toUpperCase()
        + fieldName.substring(1);

    String sb = "public " + getClassName(fieldClass) + " "
        + getterName + "(){" + "return this."
        + fieldName + ";" + "}";
    return CtMethod.make(sb, declaringClass);
  }

  private static String getClassName(Class clazz) {
    String toReturn = clazz.getName();
    if (toReturn.startsWith("["))
      toReturn = clazz.getSimpleName();

    return toReturn;
  }

  private static CtMethod generateSetter(CtClass declaringClass, String methodName, String fieldName, Class fieldClass)
      throws CannotCompileException {

    if (methodName == null) {
      methodName = fieldName;
    }
    String setterName = "set" + methodName.substring(0, 1).toUpperCase()
        + methodName.substring(1);

    String sb = "public void " + setterName + "("
        + getClassName(fieldClass) + " " + fieldName
        + ")" + "{" + "this." + fieldName
        + "=" + fieldName + ";" + "}";
    return CtMethod.make(sb, declaringClass);
  }

  private static EdmSimpleTypeKind toEdmSimpleTypeKind(Class fieldClass) {
    try {
      return JPATypeConverter.convertToEdmSimpleType(fieldClass, null);
    } catch (ODataJPAModelException e) {
      return EdmSimpleTypeKind.String;
    }
  }

  private EntityType findEntityType(Schema edmSchema, String entity) {
    for (EntityType type : edmSchema.getEntityTypes()) {
      if (type.getName().equals(entity)) {
        return type;
      }
    }

    return null;
  }

  private ReportItem findReportItem(ReportQuery reportQuery, String name) {
    for (ReportItem item : reportQuery.getItems()) {
      if (item.getName().equals(name)) {
        return item;
      }
    }

    return null;
  }

  private Property findProperty(EntityType entityType, String name) {
    for (Property item : entityType.getProperties()) {
      if (item.getName().equals(name)) {
        return item;
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

  private void createDataSource(Schema edmSchema, String id, String entity) {

    String edmNamespace = edmSchema.getNamespace();
    EntityManagerImpl em = (EntityManagerImpl) context.getEntityManager();
    JsonObject queryJson = QueryManager.getQuery(id);
    String jpql = QueryManager.getJPQL(queryJson);
    AbstractSession session = em.getActiveSessionIfExists();

    HermesParser parser = new HermesParser();
    Object query = parser.buildQuery(jpql, session);

    if (query instanceof ReportQuery) {

      ReportQuery reportQuery = (ReportQuery) parser.buildQuery(jpql, session);
      reportQuery.prepareInternal(session);

      if (reportQuery.getItems().size() == 1 && reportQuery.getItems().get(0).getDescriptor() != null) {
        entity = reportQuery.getItems().get(0).getDescriptor().getJavaClass().getSimpleName();
        createEntityDataSource(edmSchema, id, entity);
      } else {


        JPQLExpression jpqlExpression = new JPQLExpression(
            jpql,
            DefaultEclipseLinkJPQLGrammar.instance(),
            true
        );

        String mainEntity = JPQLParserUtil.getMainEntity(jpqlExpression);

        EntityType mainType = findEntityType(edmSchema, mainEntity);

        EntitySet set = new EntitySet();
        set.setName(id);
        set.setEntityType(new FullQualifiedName(edmNamespace, id));

        edmSchema.getEntityContainers().get(0).getEntitySets().add(set);

        ListIterable<Expression> children = ((SelectClause) ((SelectStatement) jpqlExpression.getQueryStatement()).getSelectClause()).getSelectExpression().children();
        ListIterator<Expression> expressions = children.iterator();
        Key key = new Key();
        List<PropertyRef> propertyRefList = new ArrayList<>();
        key.setKeys(propertyRefList);

        List<Property> properties = new ArrayList<>();
        boolean keysSet = false;
        for (ReportItem item : reportQuery.getItems()) {
          Expression expression = expressions.next();
          if (expression instanceof IdentificationVariable) {
            expression = expression.getParent();
          }

          if (expression instanceof ResultVariable) {
            expression = ((ResultVariable) expression).getSelectExpression();
          }
          SimpleProperty property = new SimpleProperty();
          Class type = Object.class;
          if (item.getMapping() != null) {
            type = item.getMapping().getField().getType();
          } else if (item.getDescriptor() != null) {
            type = item.getDescriptor().getJavaClass();
          }

          property.setType(toEdmSimpleTypeKind(type));
          property.setName(item.getName());

          JPAEdmMappingImpl mapping = new JPAEdmMappingImpl();
          mapping.setInternalExpression(expression.toString());
          mapping.setInternalName(item.getName());
          mapping.setJPAType(type);
          mapping.setVirtualAccess(true);

          property.setMapping(mapping);

          properties.add(property);

          if (findKey(mainType, item.getName()) != null) {
            PropertyRef propertyRef = new PropertyRef();
            propertyRef.setName(item.getName());
            propertyRefList.add(propertyRef);
            keysSet = true;
          }

        }

        boolean canEdit = true;
        if (!keysSet || propertyRefList.size() != mainType.getKey().getKeys().size()) {
          propertyRefList.clear();
          canEdit = false;
          for (ReportItem item : reportQuery.getItems()) {
            PropertyRef propertyRef = new PropertyRef();
            propertyRef.setName(item.getName());
            propertyRefList.add(propertyRef);
          }
        }

        EntityType type = new EntityType();

        type.setProperties(properties);
        type.setKey(key);
        type.setName(id);
        JPAEdmMappingImpl mapping = new JPAEdmMappingImpl();
        mapping.setODataJPATombstoneEntityListener(QueryExtensionEntityListener.class);
        mapping.setCanEdit(canEdit);
        mapping.setJPAType(((JPAEdmMappingImpl) mainType.getMapping()).getJPAType());
        if (canEdit) {
          mapping.setInternalName(mainEntity);
        }
        type.setMapping(mapping);

        edmSchema.getEntityTypes().add(type);
      }
    } else {
      createEntityDataSource(edmSchema, id, entity);
    }
  }

  private void createEntityDataSource(Schema edmSchema, String id, String entity) {

    for (EntityContainer container : edmSchema.getEntityContainers()) {

      EntitySet foundES = null;

      for (EntitySet entitySet : container.getEntitySets()) {
        if (entitySet.getEntityType().getName().equals(entity)) {
          foundES = entitySet;
          break;
        }
      }

      EntitySet set = new EntitySet();
      set.setName(id);
      set.setEntityType(foundES.getEntityType());
      set.setMapping(foundES.getMapping());
      set.setAnnotationAttributes(foundES.getAnnotationAttributes());
      set.setAnnotationElements(foundES.getAnnotationElements());

      container.getEntitySets().add(set);

      List<AssociationSet> addAssociationSet = new LinkedList<>();

      for (AssociationSet association : container.getAssociationSets()) {
        if (association.getEnd1().getRole().equals(entity)) {

          AssociationSet newAssociation = new AssociationSet();
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
    }
  }

  @Override
  public InputStream getJPAEdmMappingModelStream() {
    return null;
  }
}
