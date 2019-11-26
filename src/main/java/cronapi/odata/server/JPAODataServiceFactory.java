package cronapi.odata.server;

import cronapi.ErrorResponse;
import cronapi.RestClient;
import cronapi.database.TransactionManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;

import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAServiceFactory;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;

public class JPAODataServiceFactory extends ODataJPAServiceFactory {

  private final EntityManagerFactory entityManagerFactory;
  private final String namespace;
  private int order;

  public JPAODataServiceFactory(EntityManagerFactory entityManagerFactory, String namespace, int order) {
    this.entityManagerFactory = entityManagerFactory;
    this.namespace = namespace;
    this.order = order;
  }

  @Override
  public ODataJPAContext initializeODataJPAContext() throws ODataJPARuntimeException {
    ODataJPAContext context = getODataJPAContext();
    context.setEntityManagerFactory(entityManagerFactory);
    context.setPersistenceUnitName(namespace);

    TransactionManager.addNamespace(namespace, context.getEntityManager());

    context.getEntityManager().setFlushMode(FlushModeType.COMMIT);
    context.setJPAEdmExtension(new DatasourceExtension(context, order));
    context.setoDataJPAQueryExtensionEntityListener(new QueryExtensionEntityListener());

    return context;
  }

  @Override
  public ODataService createODataSingleProcessorService(EdmProvider provider, ODataSingleProcessor processor) {
    return super.createODataSingleProcessorService(provider, processor);
  }

  @Override
  public Exception handleException(Throwable throwable, UriInfo info) {
    String id = null;
    try {
      final EdmEntitySet oDataEntitySet = info.getTargetEntitySet();
      final EdmEntityType entityType = oDataEntitySet.getEntityType();
      id = entityType.getName();
    } catch (Exception e) {
      //Abafa
    }

    String msg = ErrorResponse.getExceptionMessage(throwable, RestClient.getRestClient() != null ? RestClient.getRestClient().getMethod() : "GET", id);
    return new RuntimeException(msg, throwable);
  }
}
