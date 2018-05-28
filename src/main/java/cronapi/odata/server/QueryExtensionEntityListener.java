package cronapi.odata.server;

import com.google.gson.JsonObject;
import cronapi.CronapiFilter;
import cronapi.QueryManager;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.api.uri.info.*;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAQueryExtensionEntityListener;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.core.ODataExpressionParser;
import org.apache.olingo.odata2.jpa.processor.core.ODataParameterizedWhereExpressionUtil;
import org.eclipse.persistence.jpa.jpql.parser.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class QueryExtensionEntityListener extends ODataJPAQueryExtensionEntityListener {


  public Query getBaseQuery(UriInfo uriInfo, EntityManager em) throws ODataJPARuntimeException {

    HttpServletRequest request = CronapiFilter.REQUEST.get();
    try {
      JsonObject customQuery = null;

      try {
        customQuery = QueryManager.getQuery(uriInfo.getTargetEntitySet().getName());
      } catch (Exception e) {
        //No Command
      }

      if (customQuery != null) {

        String whereExpression = null;

        Query query = null;

        String jpqlStatement = QueryManager.getJPQL(customQuery);

        JPQLExpression jpqlExpression = new JPQLExpression(
            jpqlStatement,
            DefaultEclipseLinkJPQLGrammar.instance(),
            true
        );

        SelectStatement selectStatement = ((SelectStatement) jpqlExpression.getQueryStatement());
        String selection = ((SelectClause) selectStatement.getSelectClause()).getSelectExpression().toActualText();

        String alias = null;

        if (!selection.contains(".") && !selection.contains(",")) {
          IdentificationVariableDeclaration identificationVariableDeclaration = ((IdentificationVariableDeclaration) ((FromClause) selectStatement.getFromClause()).getDeclaration());
          if (!identificationVariableDeclaration.hasJoins()) {
            RangeVariableDeclaration rangeVariableDeclaration = (RangeVariableDeclaration) identificationVariableDeclaration.getRangeVariableDeclaration();
            alias = rangeVariableDeclaration.getIdentificationVariable().toActualText();
          }
        }

        if (uriInfo.isCount()) {
          setField(selectStatement, "selectClause", null);
          jpqlStatement = "SELECT count(" + alias + ") " + selectStatement.toString();

          jpqlExpression = new JPQLExpression(
              jpqlStatement,
              DefaultEclipseLinkJPQLGrammar.instance(),
              true
          );
          selectStatement = ((SelectStatement) jpqlExpression.getQueryStatement());
          selection = ((SelectClause) selectStatement.getSelectClause()).getSelectExpression().toActualText();

          if (selectStatement.hasOrderByClause()) {
            setField(selectStatement, "orderByClause", null);
            jpqlStatement = selectStatement.toString();
          }
        }

        String orderBy = null;

        if (selectStatement.hasOrderByClause()) {
          orderBy = selectStatement.getOrderByClause().toString();
          setField(selectStatement, "orderByClause", null);
          jpqlStatement = selectStatement.toString();
        }

        if (uriInfo.getOrderBy() != null) {
          String orderExpression = ODataExpressionParser.parseToJPAOrderByExpression(uriInfo.getOrderBy(), alias);
          orderBy = "ORDER BY " + orderExpression;
        }


        ODataExpressionParser.reInitializePositionalParameters();
        Map<String, Map<Integer, Object>> parameterizedExpressionMap =
            new HashMap<String, Map<Integer, Object>>();

        if (uriInfo.getFilter() != null) {
          whereExpression = ODataExpressionParser.parseToJPAWhereExpression(
              uriInfo.getFilter(), alias);
          parameterizedExpressionMap.put(whereExpression, ODataExpressionParser.getPositionalParameters());
          ODataParameterizedWhereExpressionUtil.setParameterizedQueryMap(parameterizedExpressionMap);
          ODataExpressionParser.reInitializePositionalParameters();
        }

        String where = null;
        String having = null;
        String groupBy = null;

        if (whereExpression != null) {

          if (selectStatement.hasWhereClause()) {
            where = ((WhereClause) selectStatement.getWhereClause()).getConditionalExpression().toString();
            setField(selectStatement, "whereClause", null);
            jpqlStatement = selectStatement.toString();
          }

          if (selectStatement.hasGroupByClause()) {
            groupBy = ((GroupByClause) selectStatement.getGroupByClause()).toString();
            setField(selectStatement, "groupByClause", null);
            jpqlStatement = selectStatement.toString();
          }

          if (selectStatement.hasHavingClause()) {
            having = ((HavingClause) selectStatement.getHavingClause()).toString();
            setField(selectStatement, "havingClause", null);
            jpqlStatement = selectStatement.toString();
          }

          if (where != null) {
            jpqlStatement += " WHERE (" + where + ") AND " + whereExpression;
          } else {
            jpqlStatement += " WHERE " + whereExpression;
          }

          if (having != null) {
            jpqlStatement += " " + having;
          }

          if (groupBy != null) {
            jpqlStatement += " " + groupBy;
          }
        }

        if (orderBy != null) {
          jpqlStatement += " " + orderBy;
        }

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

        // }
      /*  } else {
          if (orderBy != null) {
            jpqlStatement += " " + orderBy;
          }
          query = em.createQuery(jpqlStatement);
        }*/

        return query;

      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return null;
  }

  @Override
  public Query getQuery(GetEntitySetUriInfo uriInfo, EntityManager em) throws ODataJPARuntimeException {
    return this.getBaseQuery((UriInfo) uriInfo, em);
  }

  @Override
  public Query getQuery(GetEntityCountUriInfo uriInfo, EntityManager em) throws ODataJPARuntimeException {
    return this.getBaseQuery((UriInfo) uriInfo, em);
  }

  @Override
  public Query getQuery(GetEntitySetCountUriInfo uriInfo, EntityManager em) throws ODataJPARuntimeException {
    return this.getBaseQuery((UriInfo) uriInfo, em);
  }

  @Override
  public Query getQuery(GetEntityUriInfo uriInfo, EntityManager em) throws ODataJPARuntimeException {
    return this.getBaseQuery((UriInfo) uriInfo, em);
  }

  private void setField(Object obj, String name, Object value) {
    try {

      Field field;
      try {
        field = obj.getClass().getDeclaredField(name);
      } catch (Exception e) {
        field = obj.getClass().getSuperclass().getDeclaredField(name);
      }

      if (field != null) {
        field.setAccessible(true);
        field.set(obj, value);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Object getField(Object obj, String name) {
    try {
      Field field;
      try {
        field = obj.getClass().getDeclaredField(name);
      } catch (Exception e) {
        field = obj.getClass().getSuperclass().getDeclaredField(name);
      }

      if (field != null) {
        field.setAccessible(true);
        return field.get(obj);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return null;
  }
}
