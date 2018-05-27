package cronapi.odata.server;

import com.google.gson.JsonObject;
import cronapi.CronapiFilter;
import cronapi.QueryManager;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetUriInfo;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAQueryExtensionEntityListener;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.core.ODataExpressionParser;
import org.apache.olingo.odata2.jpa.processor.core.ODataParameterizedWhereExpressionUtil;
import org.eclipse.persistence.internal.jpa.QueryHintsHandler;
import org.eclipse.persistence.jpa.jpql.parser.JPQLExpression;
import org.eclipse.persistence.jpa.jpql.parser.SelectStatement;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.servlet.http.HttpServletRequest;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class QueryExtensionEntityListener extends ODataJPAQueryExtensionEntityListener {


  public Query getQuery(GetEntitySetUriInfo uriInfo, EntityManager em) throws ODataJPARuntimeException {

    HttpServletRequest request = CronapiFilter.REQUEST.get();
    try {
      JsonObject customQuery = QueryManager.getQuery(uriInfo.getTargetEntitySet().getName());
      if (customQuery != null) {

        String whereExpression = "";

        Query query = null;

        String jpqlStatement = QueryManager.getJPQL(customQuery);

        JPQLExpression jpqlExpression = (JPQLExpression) uriInfo.getTargetEntitySet().getEntityType().getMapping().getObject();

        if (uriInfo.getFilter() != null) {
          ODataExpressionParser.reInitializePositionalParameters();
          whereExpression = ODataExpressionParser.parseToJPAWhereExpression(
              uriInfo.getFilter(), null);

          Map<String, Map<Integer, Object>> parameterizedExpressionMap =
              new HashMap<String, Map<Integer, Object>>();
          parameterizedExpressionMap.put(whereExpression, ODataExpressionParser.getPositionalParameters());
          ODataParameterizedWhereExpressionUtil.setParameterizedQueryMap(parameterizedExpressionMap);
          ODataExpressionParser.reInitializePositionalParameters();

          if (((SelectStatement) jpqlExpression.getQueryStatement()).hasWhereClause()) {
            jpqlStatement += " AND ";
          } else {
            jpqlStatement += " WHERE ";
          }

          jpqlStatement += whereExpression;

          query = em.createQuery(jpqlStatement);

          Map<String, Map<Integer, Object>> parameterizedMap = ODataParameterizedWhereExpressionUtil.
              getParameterizedQueryMap();
          if (parameterizedMap != null && parameterizedMap.size() > 0) {
            for (Map.Entry<String, Map<Integer, Object>> parameterEntry : parameterizedMap.entrySet()) {
              if (jpqlStatement.contains(parameterEntry.getKey())) {
                Map<Integer, Object> positionalParameters = parameterEntry.getValue();
                for (Map.Entry<Integer, Object> param : positionalParameters.entrySet()) {
                  if (param.getValue() instanceof Calendar || param.getValue() instanceof Timestamp) {
                    query.setParameter(param.getKey(), (Calendar) param.getValue(), TemporalType.TIMESTAMP);
                  } else if (param.getValue() instanceof Time) {
                    query.setParameter(param.getKey(), (Time) param.getValue(), TemporalType.TIME);
                  } else {
                    query.setParameter(param.getKey(), param.getValue());
                  }
                }
                parameterizedMap.remove(parameterEntry.getKey());
                ODataParameterizedWhereExpressionUtil.setJPQLStatement(null);
                break;
              }
            }
          }
        } else {
          query = em.createQuery(jpqlStatement);
        }

        return query;


      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }
}
