package cronapi.odata.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cronapi.QueryManager;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.jpa.metadata.api.JPAEdmExtension;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.IntermediateEntitySet;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.IntermediateEntityType;
import org.apache.olingo.jpa.processor.core.api.JPAODataGetHandler;
import org.apache.olingo.jpa.processor.core.database.JPADefaultDatabaseProcessor;
import org.apache.olingo.jpa.processor.core.mapping.JPAPersistenceAdapter;
import org.apache.olingo.jpa.processor.core.mapping.ResourceLocalPersistenceAdapter;
import org.apache.olingo.server.api.processor.Processor;
import org.eclipse.persistence.internal.jpa.parsing.jpql.JPQLParser;
import org.eclipse.persistence.jpa.JpaQuery;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;
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
        JsonObject queries = QueryManager.getJSON();

        for (Map.Entry<String, JsonElement> entry : queries.entrySet()) {
          JsonObject customObj = entry.getValue().getAsJsonObject();
          if (!QueryManager.isNull(customObj.get("entityFullName"))) {
            String clazz = customObj.get("entityFullName").getAsString();
            if (clazz.startsWith(namespace)) {
              try {
                createDataSource(set, entry.getKey(), clazz);
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          }
        }
      }

      @Override
      public TypedQuery createQuery(EntityManager em, CriteriaQuery<Tuple> criteria) {
        JPQLParser parser = JPQLParser.buildParserFor("select c from Teste c where c.Id = 'asdsad' ");
        parser.parse();


        //parser.getParseTree().getWhereNode()

        //criteria.
      //  TypedQuery<Tuple> query = em.createQuery(

        //    "select c.id as id, c.id2 as id2 from Teste c", Tuple.class);

       // List<Tuple> results = query.getResultList();

        TypedQuery<Tuple> xy = em.createQuery(criteria);

        String jpql = xy.unwrap(JpaQuery.class).getDatabaseQuery().getJPQLString();

        return em.createQuery(criteria);

        //return super.createQuery(criteria);
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
