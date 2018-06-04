package cronapi.odata.server;

import cronapi.util.Operations;
import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAServiceFactory;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.factory.ODataJPAAccessFactory;
import org.apache.olingo.odata2.jpa.processor.api.factory.ODataJPAFactory;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmExtension;

import javax.persistence.EntityManagerFactory;

public class JpaOdataServiceFactory extends ODataJPAServiceFactory {

  private final EntityManagerFactory entityManagerFactory;
  private final String namespace;

  private static ODataService oDataService;
  private static DatasourceExtension datasourceExtension;
  private static QueryExtensionEntityListener queryExtensionEntityListener;

  public JpaOdataServiceFactory(EntityManagerFactory entityManagerFactory, String namespace) {
    this.entityManagerFactory = entityManagerFactory;
    this.namespace = namespace;
  }

  @Override
  public ODataJPAContext initializeODataJPAContext() throws ODataJPARuntimeException {
    ODataJPAContext context = getODataJPAContext();
    context.setEntityManagerFactory(entityManagerFactory);
    context.setPersistenceUnitName(namespace);

    if (Operations.IS_DEBUG) {
      context.setJPAEdmExtension(new DatasourceExtension(context));
      context.setoDataJPAQueryExtensionEntityListener(new QueryExtensionEntityListener());
    } else {
      if (datasourceExtension == null) {
        synchronized (JpaOdataServiceFactory.this) {
          if (datasourceExtension == null) {
            datasourceExtension = new DatasourceExtension(context);
            queryExtensionEntityListener = new QueryExtensionEntityListener();
          }
        }
      }

      context.setJPAEdmExtension(datasourceExtension);
      context.setoDataJPAQueryExtensionEntityListener(queryExtensionEntityListener);
    }

    return context;
  }

  @Override
  public ODataService createODataSingleProcessorService(EdmProvider provider, ODataSingleProcessor processor) {
    if (Operations.IS_DEBUG) {
      return super.createODataSingleProcessorService(provider, processor);
    }

    if (oDataService == null) {
      synchronized (JpaOdataServiceFactory.this) {
        if (oDataService == null) {
          oDataService = super.createODataSingleProcessorService(provider, processor);
        }
      }
    }

    return oDataService;
  }
}
