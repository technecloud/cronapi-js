package cronapi.odata.server;

import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.*;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmExtension;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmSchemaView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DatasourceExtension implements JPAEdmExtension {
  @Override
  public void extendWithOperation(JPAEdmSchemaView jpaEdmSchemaView) {
    jpaEdmSchemaView.registerOperations(DatasourceOperations.class, null);
  }

  @Override
  public void extendJPAEdmSchema(JPAEdmSchemaView view) {
    Schema edmSchema = view.getEdmSchema();
  /*  List<ComplexType> types = new ArrayList<>();
    types.add(getComplexType());
    edmSchema.setComplexTypes(types);


    EntityType old = edmSchema.getEntityTypes().get(0);


    EntityType type = new EntityType();
    type.setName("Thiago");
    type.setMapping(old.getMapping());
    type.setKey(old.getKey());
    type.setProperties(old.getProperties());
    type.setAbstract(old.isAbstract());
    type.setAnnotationAttributes(old.getAnnotationAttributes());
    type.setAnnotationElements(old.getAnnotationElements());
    type.setBaseType(old.getBaseType());
    type.setDocumentation(old.getDocumentation());
    type.setCustomizableFeedMappings(old.getCustomizableFeedMappings());
    type.setNavigationProperties(old.getNavigationProperties());
    type.setHasStream(old.isHasStream());*/

//    edmSchema.getEntityTypes().add(type);

  /*  EntitySet oldSet = edmSchema.getEntityContainers().get(0).getEntitySets().get(0);

    EntitySet set = new EntitySet();
    set.setName("jose");
    set.setEntityType(oldSet.getEntityType());
    set.setMapping(oldSet.getMapping());
    set.setAnnotationAttributes(oldSet.getAnnotationAttributes());
    set.setAnnotationElements(oldSet.getAnnotationElements());

    AssociationSet oldAss = edmSchema.getEntityContainers().get(0).getAssociationSets().get(0);

    AssociationSet associationSet = new AssociationSet();
    associationSet.setName("TESTTE");
    associationSet.setAnnotationAttributes(oldAss.getAnnotationAttributes());
    associationSet.setAnnotationElements(oldAss.getAnnotationElements());
    associationSet.setAssociation(oldAss.getAssociation());
    AssociationSetEnd xy = new AssociationSetEnd();
    xy.setEntitySet("jose");
    xy.setRole(oldAss.getEnd1().getRole());
    associationSet.setEnd1(xy);
    associationSet.setEnd2(oldAss.getEnd2());



    edmSchema.getEntityContainers().get(0).getEntitySets().add(set);
   edmSchema.getEntityContainers().get(0).getAssociationSets().add(associationSet);*/





   // edmSchema.getComplexTypes().add(getComplexType());

    createDataSource(edmSchema, "thiago", "Company");

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

      for ( AssociationSet association: container.getAssociationSets()) {
        if (association.getEnd1().getRole().equals(entity)) {

          AssociationSet newAssociation = new AssociationSet();
          newAssociation.setName(association.getName()+"_"+id);
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

      for (AssociationSet association: addAssociationSet) {
        container.getAssociationSets().add(association);
      }
    }
  }

  @Override
  public InputStream getJPAEdmMappingModelStream() {
    return null;
  }


  private ComplexType getComplexType() {
    ComplexType complexType = new ComplexType();

    List<Property> properties = new ArrayList<>();
    SimpleProperty property = new SimpleProperty();

    property.setName("Amount");
    property.setType(EdmSimpleTypeKind.Double);
    properties.add(property);

    property = new SimpleProperty();
    property.setName("Currency");
    property.setType(EdmSimpleTypeKind.String);
    properties.add(property);

    complexType.setName("Object");
    complexType.setProperties(properties);

    return complexType;

  }
}
