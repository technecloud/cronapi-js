package cronapi.odata.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cronapi.QueryManager;
import org.apache.olingo.odata2.api.edm.provider.*;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmExtension;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmMapping;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmSchemaView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DatasourceExtension implements JPAEdmExtension {
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
            createDataSource(edmSchema, entry.getKey(), customObj.get("entitySimpleName").getAsString());
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

  private void createDataSource(Schema edmSchema, String id, String entity) {

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

  /*  EntityType foundET = null;

    for (EntityType entityType : edmSchema.getEntityTypes()) {
      if (entityType.getName().equals(entity)) {
        foundET = entityType;
        break;
      }
    }

    EntityType type = new EntityType();
    type.setHasStream(foundET.isHasStream());
    type.setNavigationProperties(foundET.getNavigationProperties());
    type.setCustomizableFeedMappings(foundET.getCustomizableFeedMappings());
    type.setDocumentation(foundET.getDocumentation());
    type.setBaseType(foundET.getBaseType());
    type.setAnnotationElements(foundET.getAnnotationElements());
    type.setProperties(foundET.getProperties());

    Key key = new Key();
    List<PropertyRef> props = new ArrayList<>();
    PropertyRef p = new PropertyRef();
    p.setName("teste");
    props.add(p);
    type.setKey(key);

    type.setMapping(foundET.getMapping());
    type.setAnnotationAttributes(foundET.getAnnotationAttributes());
    type.setName(id);

    edmSchema.getEntityTypes().add(type);*/
  }

  @Override
  public InputStream getJPAEdmMappingModelStream() {
    return null;
  }
}
