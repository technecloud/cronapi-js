package cronapi.odata.server;

import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAServiceFactory;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmExtension;

import javax.persistence.EntityManagerFactory;

public class JpaOdataServiceFactory extends ODataJPAServiceFactory {

  private final EntityManagerFactory entityManagerFactory;
  private final String namespace;

  public JpaOdataServiceFactory(EntityManagerFactory entityManagerFactory, String namespace) {
    this.entityManagerFactory = entityManagerFactory;
    this.namespace = namespace;
  }

  @Override
  public ODataJPAContext initializeODataJPAContext() throws ODataJPARuntimeException {
    ODataJPAContext context = getODataJPAContext();
    context.setEntityManagerFactory(entityManagerFactory);
    context.setPersistenceUnitName(namespace);
    context.setJPAEdmExtension((JPAEdmExtension) new DatasourceExtension(context));
    return context;
  }

}
