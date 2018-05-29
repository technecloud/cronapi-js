package cronapi.odata.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cronapi.QueryManager;
import cronapi.clazz.CronapiClassLoader;
import cronapi.util.Operations;
import javassist.*;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;

public class DatasourceExtension implements JPAEdmExtension {

  private final ODataJPAContext context;

  public static final ThreadLocal<ClassLoader> LOADER = new ThreadLocal<ClassLoader>();

  public DatasourceExtension(ODataJPAContext context) {
    this.context = context;
    if (Operations.IS_DEBUG) {
      LOADER.set(new CronapiClassLoader());
    } else {
      LOADER.set(DatasourceExtension.class.getClassLoader());
    }
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

  public static ClassLoader getClassLoader() {
    if (!Operations.IS_DEBUG) {
      return DatasourceExtension.class.getClassLoader();
    } else {
      return LOADER.get();
    }
  }

  private EntityType findEntityType(Schema edmSchema, String entity) {
    for (EntityType type: edmSchema.getEntityTypes()) {
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

        String alias = "";
        String mainEntity = "";

        JPQLExpression jpqlExpression = new JPQLExpression(
            jpql,
            DefaultEclipseLinkJPQLGrammar.instance(),
            true
        );

        SelectStatement selectStatement = ((SelectStatement) jpqlExpression.getQueryStatement());

        IdentificationVariableDeclaration identificationVariableDeclaration = ((IdentificationVariableDeclaration) ((FromClause) selectStatement.getFromClause()).getDeclaration());
        if (!identificationVariableDeclaration.hasJoins()) {
          RangeVariableDeclaration rangeVariableDeclaration = (RangeVariableDeclaration) identificationVariableDeclaration.getRangeVariableDeclaration();
          alias = rangeVariableDeclaration.getIdentificationVariable().toActualText();
          mainEntity = rangeVariableDeclaration.getRootObject().toString();
        }

        EntityType mainType = findEntityType(edmSchema, mainEntity);

        ClassLoader classLoader = getClassLoader();

        boolean createClass = false;
        try {
          classLoader.loadClass(edmNamespace + "." + id);
        } catch (ClassNotFoundException e) {
          createClass = true;
        }

        if (createClass) {

          ClassPool pool = ClassPool.getDefault();
          pool.appendClassPath(new LoaderClassPath(DatasourceExtension.class.getClassLoader()));
          CtClass cc;
          try {
            cc = pool.get(edmNamespace + "." + id);
             if (Operations.IS_DEBUG)
               cc.detach();
          } catch (NotFoundException e) {
            //No Command
          }
          cc = pool.makeClass(edmNamespace + "." + id);
          try {
            cc.addInterface(pool.get(Serializable.class.getName()));

            int i = 0;

            for (ReportItem item : reportQuery.getItems()) {
              String typeName = "java.lang.Object";
              Class type = Object.class;

              if (item.getMapping() != null) {
                typeName = item.getMapping().getField().getTypeName();
                type = item.getMapping().getField().getType();
              } else if (item.getDescriptor() != null) {
                typeName = item.getDescriptor().getJavaClassName();
                type = item.getDescriptor().getJavaClass();
              }
              cc.addField(new CtField(pool.get(typeName), item.getName(), cc));
              cc.addMethod(generateGetter(cc, item.getName(), type));
              cc.addMethod(generateSetter(cc, null, item.getName(), type));
              cc.addMethod(generateSetter(cc, "CronAppParam" + i, item.getName(), type));
              i++;
            }

            if (classLoader instanceof CronapiClassLoader) {
              ((CronapiClassLoader) classLoader).addClass(edmNamespace + "." + id, cc.toBytecode());
            } else {
              cc.toClass(this.getClass().getClassLoader(), this.getClass().getProtectionDomain());
            }
          } catch (CannotCompileException | IOException | NotFoundException e1) {
            e1.printStackTrace();
          }
        }

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

          property.setMapping(mapping);

          properties.add(property);

          if (findKey(mainType, item.getName()) != null) {
            PropertyRef propertyRef = new PropertyRef();
            propertyRef.setName(item.getName());
            propertyRefList.add(propertyRef);
          }

         // if (findProperty(mainType, item.getName()) == null) {
            mapping.setVirtualAccess(true);
          //}
        }

        EntityType type = new EntityType();

        type.setProperties(properties);
        type.setKey(key);
        type.setName(id);
        JPAEdmMappingImpl mapping = new JPAEdmMappingImpl();
        mapping.setODataJPATombstoneEntityListener(QueryExtensionEntityListener.class);
        mapping.setCanEdit(false);
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
