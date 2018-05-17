package cronapi.odata.server;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.jpa.metadata.api.JPAEdmExtension;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.IntermediateEntitySet;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.IntermediateEntityType;
import org.apache.olingo.jpa.processor.core.api.JPAODataGetHandler;
import org.apache.olingo.jpa.processor.core.database.JPADefaultDatabaseProcessor;
import org.apache.olingo.jpa.processor.core.mapping.JPAPersistenceAdapter;
import org.apache.olingo.jpa.processor.core.mapping.ResourceLocalPersistenceAdapter;
import org.apache.olingo.server.api.processor.Processor;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Map;


public class ODataServletV4 extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private EntityManagerFactory entityManagerFactory;
  private String namespace;

  ODataServletV4(EntityManagerFactory entityManagerFactory, String namespace) {
    this.entityManagerFactory = entityManagerFactory;
    this.namespace = namespace;
  }

  @Override
  protected void service(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException {

    try {
      JPAODataGetHandler handler = createHandler();
      handler.process(req, resp);
    } catch (final ODataException e) {
      throw new ServletException(e);
    }

  }

  private JPAODataGetHandler createHandler() throws ODataException {

    final JPAPersistenceAdapter mappingAdapter = new ResourceLocalPersistenceAdapter(namespace,
        entityManagerFactory,
        new JPADefaultDatabaseProcessor());
    final JPAODataGetHandler handler = new JPAODataGetHandler(mappingAdapter) {
      @Override
      protected Collection<Processor> collectProcessors(final HttpServletRequest request,
                                                        final HttpServletResponse response, final EntityManager em) {
        final Collection<Processor> processors = super.collectProcessors(request, response, em);
        processors.add(new ODataErrorProcessor());
        return processors;
      }
    };

    handler.setJPAEdmExtension(new JPAEdmExtension() {
      @Override
      public void extendJPAEntitySet(Map<String, IntermediateEntitySet> set) {
        try {
          createDataSource(set, "Thiago", "app.entity.Company");
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });

    return handler;
  }

  private void createDataSource(Map<String, IntermediateEntitySet> set, String id, String entityNamespace) throws Exception {

    IntermediateEntitySet foundES = null;

    for (IntermediateEntitySet entity : set.values()) {
      if (entity.getEntityType().getTypeClass().getName().equals(entityNamespace)) {
        foundES = entity;
        break;
      }
    }

    if (foundES != null) {
      IntermediateEntitySet newSet = new IntermediateEntitySet(foundES.getNameBuilder(), (IntermediateEntityType) foundES.getEntityType());
      newSet.setExternalName(id);
      set.put(foundES.getInternalName() + "_" + id, newSet);
    }
  }


}
