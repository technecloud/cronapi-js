package cronapi.odata.server;

import com.google.gson.JsonObject;
import cronapi.CronapiFilter;
import cronapi.QueryManager;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetUriInfo;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAQueryExtensionEntityListener;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;

public class QueryExtensionEntityListener extends ODataJPAQueryExtensionEntityListener {


  public Query getQuery(GetEntitySetUriInfo uriInfo, EntityManager em) throws ODataJPARuntimeException {

    HttpServletRequest request = CronapiFilter.REQUEST.get();
    try {
      JsonObject query = QueryManager.getQuery(uriInfo.getTargetEntitySet().getName());
      if (query != null) {

        //return em.createQuery(QueryManager.getJPQL(query));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }
}
