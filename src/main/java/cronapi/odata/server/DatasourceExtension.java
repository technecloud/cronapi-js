package cronapi.odata.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cronapi.QueryManager;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.EntitySet;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.Key;
import org.apache.olingo.odata2.api.edm.provider.Mapping;
import org.apache.olingo.odata2.api.edm.provider.Property;
import org.apache.olingo.odata2.api.edm.provider.PropertyRef;
import org.apache.olingo.odata2.api.edm.provider.Schema;
import org.apache.olingo.odata2.api.edm.provider.SimpleProperty;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmExtension;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmMapping;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmSchemaView;
import org.eclipse.persistence.internal.jpa.EntityManagerImpl;
import org.eclipse.persistence.internal.jpa.jpql.HermesParser;
import org.eclipse.persistence.internal.queries.ReportItem;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.queries.ReportQuery;

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

  private static String getClassName(Class clazz)
  {
    String toReturn = clazz.getName();
    if (toReturn.startsWith("["))
      toReturn = clazz.getSimpleName();

    return toReturn;
  }

  private static CtMethod generateSetter(CtClass declaringClass, String fieldName, Class fieldClass)
      throws CannotCompileException {

    String setterName = "set" + fieldName.substring(0, 1).toUpperCase()
        + fieldName.substring(1);

    String sb = "public void " + setterName + "("
        + getClassName(fieldClass) + " " + fieldName
        + ")" + "{" + "this." + fieldName
        + "=" + fieldName + ";" + "}";
    return CtMethod.make(sb, declaringClass);
  }

  private static EdmSimpleTypeKind toEdmSimpleTypeKind(Class fieldClass)
  {
    return EdmSimpleTypeKind.String;
  }

  private void createDataSource(Schema edmSchema, String id, String entity) {
    String edmNamespace = edmSchema.getNamespace();
    EntityManagerImpl em = (EntityManagerImpl) context.getEntityManager();
    JsonObject queryJson = QueryManager.getQuery(id);
    String jpql = QueryManager.getJPQL(queryJson);
    AbstractSession session = em.getActiveSessionIfExists();

    HermesParser parser = new HermesParser();
    ReportQuery reportQuery = (ReportQuery) parser.buildQuery(jpql, session);
    reportQuery.prepareInternal(session);

    try {
      Class.forName(edmNamespace + "." + id);
    } catch(ClassNotFoundException e) {
      ClassPool pool = ClassPool.getDefault();
      CtClass cc = pool.makeClass(edmNamespace + "." + id);
      try {
        cc.addInterface(pool.get(Serializable.class.getName()));

        for (ReportItem item : reportQuery.getItems()) {
          String typeName = item.getMapping().getField().getTypeName();
          Class type = item.getMapping().getField().getType();
          cc.addField(new CtField(pool.get(typeName), item.getName(), cc));
          cc.addMethod(generateGetter(cc, item.getName(), type));
          cc.addMethod(generateSetter(cc, item.getName(), type));
        }

        cc.toClass(this.getClass().getClassLoader(), this.getClass().getProtectionDomain());
      } catch (CannotCompileException | NotFoundException e1) {
        e1.printStackTrace();
      }
      //Class.forName("MyClass");
    }

    EntitySet set = new EntitySet();
    set.setName(id);
    set.setEntityType(new FullQualifiedName(edmNamespace, id));

    edmSchema.getEntityContainers().get(0).getEntitySets().add(set);

    Key key = null;
    List<Property> properties = new ArrayList<>();
    for (ReportItem item : reportQuery.getItems()) {
      SimpleProperty property = new SimpleProperty();
      property.setType(toEdmSimpleTypeKind(item.getMapping().getField().getType()));
      property.setName(item.getName());
      properties.add(property);

      if (key == null)
      {
        key = new Key();
        PropertyRef propertyRef = new PropertyRef();
        propertyRef.setName(item.getName());
        List<PropertyRef> propertyRefList = new ArrayList<>();
        propertyRefList.add(propertyRef);
        key.setKeys(propertyRefList);
      }
    }

    EntityType type = new EntityType();

    type.setProperties(properties);
    type.setKey(key);
    type.setName(id);

    edmSchema.getEntityTypes().add(type);
  }

  @Override
  public InputStream getJPAEdmMappingModelStream() {
    return null;
  }
}
