package cronapi.odata.server;

import cronapi.Var;
import cronapi.util.ReflectionUtils;
import org.apache.olingo.odata2.jpa.processor.core.ODataExpressionParser;
import org.eclipse.persistence.internal.jpa.EJBQueryImpl;
import org.eclipse.persistence.internal.jpa.EntityManagerImpl;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.jpa.jpql.parser.*;
import org.eclipse.persistence.queries.DatabaseQuery;
import org.eclipse.persistence.sessions.DatabaseRecord;
import org.eclipse.persistence.sessions.Record;

import javax.persistence.EntityManager;
import javax.persistence.Parameter;
import javax.persistence.Query;
import java.util.LinkedList;
import java.util.List;

public class JPQLParserUtil {

  public static IdentificationVariableDeclaration getIdentificationVariableDeclaration(JPQLExpression jpqlExpression) {
    SelectStatement selectStatement = ((SelectStatement) jpqlExpression.getQueryStatement());


    Expression declaration = ((FromClause) selectStatement.getFromClause()).getDeclaration();
    IdentificationVariableDeclaration identificationVariableDeclaration = null;
    if (declaration instanceof IdentificationVariableDeclaration) {
      identificationVariableDeclaration = ((IdentificationVariableDeclaration) ((FromClause) selectStatement.getFromClause()).getDeclaration());
    }

    if (declaration instanceof CollectionExpression) {
      CollectionExpression collectionExpression = ((CollectionExpression) ((FromClause) selectStatement.getFromClause()).getDeclaration());
      identificationVariableDeclaration = (IdentificationVariableDeclaration) collectionExpression.getChild(0);
    }

    return identificationVariableDeclaration;
  }

  public static String getMainEntity(JPQLExpression jpqlExpression) {

    String mainEntity = null;

    IdentificationVariableDeclaration identificationVariableDeclaration = getIdentificationVariableDeclaration(jpqlExpression);

    if (!identificationVariableDeclaration.hasJoins()) {
      RangeVariableDeclaration rangeVariableDeclaration = (RangeVariableDeclaration) identificationVariableDeclaration.getRangeVariableDeclaration();
      mainEntity = rangeVariableDeclaration.getRootObject().toString();
    }

    return mainEntity;
  }

  public static String getMainAlias(JPQLExpression jpqlExpression) {

    String alias = null;

    IdentificationVariableDeclaration identificationVariableDeclaration = getIdentificationVariableDeclaration(jpqlExpression);

    if (!identificationVariableDeclaration.hasJoins()) {
      RangeVariableDeclaration rangeVariableDeclaration = (RangeVariableDeclaration) identificationVariableDeclaration.getRangeVariableDeclaration();
      alias = rangeVariableDeclaration.getIdentificationVariable().toString();
    }

    return alias;
  }

  public static long countAsLong(String jpql, Query query, EntityManager em) {
    Query countNativeQuery = count(jpql, query, em);
    return (long) countNativeQuery.getResultList().get(0);
  }

  public static Query count(String jpql, Query query, EntityManager em) {
    String jpqlStatement = jpql;

    JPQLExpression jpqlExpression = new JPQLExpression(
        jpqlStatement,
        DefaultEclipseLinkJPQLGrammar.instance(),
        true
    );

    SelectStatement selectStatement = ((SelectStatement) jpqlExpression.getQueryStatement());
    String selection = ((SelectClause) selectStatement.getSelectClause()).getSelectExpression().toActualText();
    String distinct = ((SelectClause) selectStatement.getSelectClause()).getActualDistinctIdentifier();
    boolean hasDistinct = ((SelectClause) selectStatement.getSelectClause()).hasDistinct();
    String mainAlias = JPQLParserUtil.getMainAlias(jpqlExpression);

    String selectExpression = null;

    ReflectionUtils.setField(selectStatement, "selectClause", null);

    if (hasDistinct) {
      selectExpression = "SELECT count(" + distinct + " " + selection + ") ";
    } else {
      selectExpression = "SELECT count(" + mainAlias + ") ";
    }

    if (selectStatement.hasOrderByClause()) {
      ReflectionUtils.setField(selectStatement, "orderByClause", null);
    }

    selectExpression += selectStatement.toString();

    Query countQuery = em.createQuery(selectExpression.toString());
    for (Parameter p : query.getParameters()) {
      if (p.getName() == null) {
        countQuery.setParameter(p.getPosition(), query.getParameterValue(p.getPosition()));
      } else {
        countQuery.setParameter(p.getName(), query.getParameterValue(p.getName()));
      }
    }

    return countQuery;
  }

  public static Query countNative(String jpql, Query query, EntityManager em) {
    AbstractSession session = (AbstractSession) ((EntityManagerImpl) em.getDelegate()).getActiveSession();

    String jpqlStatement = jpql;

    JPQLExpression jpqlExpression = new JPQLExpression(
        jpqlStatement,
        DefaultEclipseLinkJPQLGrammar.instance(),
        true
    );

    SelectStatement selectStatement = ((SelectStatement) jpqlExpression.getQueryStatement());

    if (selectStatement.hasOrderByClause()) {
      ReflectionUtils.setField(selectStatement, "orderByClause", null);
    }

    jpql = selectStatement.toString();

    Query countQuery = em.createQuery(jpql);
    LinkedList arguments = new LinkedList();
    for (Parameter p : query.getParameters()) {
      if (p.getName() == null) {
        countQuery.setParameter(p.getPosition(), query.getParameterValue(p.getPosition()));
        arguments.add(query.getParameterValue(p.getPosition()));
      } else {
        countQuery.setParameter(p.getName(), query.getParameterValue(p.getName()));
        arguments.add(query.getParameterValue(p.getName()));
      }
    }

    DatabaseQuery databaseQuery = countQuery.unwrap(EJBQueryImpl.class).getDatabaseQuery();
    databaseQuery.prepareCall(session, new DatabaseRecord());
    Record r = databaseQuery.rowFromArguments(arguments, session);
    String sql = databaseQuery.getTranslatedSQLString(session, r);
    sql = sql.replace("ESCAPE '\\'", "");

    String countSql = "select count(*) AS CRONAPP_COUNT from (" + sql + ") as CRONAPP_COUNT_SELECT";

    Query countNativeQuery = em.createNativeQuery(countSql);

    return countNativeQuery;

  }

  public static class ODataInfo {
    public String jpql;
    public Var[] params;
    public Integer first;
    public Integer max;
  }

  public static List<String> getNonWhereParams(String jpql) {
    String jpqlStatement = jpql;

    JPQLExpression jpqlExpression = new JPQLExpression(
        jpqlStatement,
        DefaultEclipseLinkJPQLGrammar.instance(),
        true
    );

    Expression selectStatement = ((Expression) jpqlExpression.getQueryStatement());


    if (selectStatement != null) {
      try {
        ReflectionUtils.setField(selectStatement, "whereClause", null);
      } catch (Exception e) {
        //NoCommand
      }
      try {
        ReflectionUtils.setField(selectStatement, "groupByClause", null);
      } catch (Exception e) {
        //NoCommand
      }
      try {
        ReflectionUtils.setField(selectStatement, "havingClause", null);
      } catch (Exception e) {
        //NoCommand
      }
      jpqlStatement = selectStatement.toString();
    }

    return parseParams(jpqlStatement);

  }

  public static ODataInfo addODdataRequest(String jpql, Var[] queryParams) {
    try {
      BlocklyQuery blocklyQuery = BlocklyQuery.CURRENT_BLOCK_QUERY.get();
      if (blocklyQuery != null) {
        String jpqlStatement = jpql;

        JPQLExpression jpqlExpression = new JPQLExpression(
            jpqlStatement,
            DefaultEclipseLinkJPQLGrammar.instance(),
            true
        );

        SelectStatement selectStatement = ((SelectStatement) jpqlExpression.getQueryStatement());
        String alias = null;
        String having = null;
        String groupBy = null;
        String orderBy = null;

        String mainAlias = JPQLParserUtil.getMainAlias(jpqlExpression);
        String selection = ((SelectClause) selectStatement.getSelectClause()).getSelectExpression().toActualText();

        if (!selection.contains(".") && !selection.contains(",")) {
          alias = mainAlias;
        }

        String whereExpression = null;

        ODataInfo info = new ODataInfo();

        if (blocklyQuery.getUriInfo().getFilter() != null || blocklyQuery.getUriInfo().getKeyPredicates().size() > 0) {

          if (blocklyQuery.getUriInfo().getFilter() != null) {
            whereExpression = ODataExpressionParser.parseToJPAWhereExpression(blocklyQuery.getUriInfo().getFilter(), alias, "?");
          }

          if (blocklyQuery.getUriInfo().getKeyPredicates().size() > 0) {
            whereExpression = ODataExpressionParser.parseKeyPredicates(blocklyQuery.getUriInfo().getKeyPredicates(), alias, "?");
          }

          List<String> params = whereExpression != null ? JPQLParserUtil.parseParams(whereExpression, '?') : null;

          if (blocklyQuery.getUriInfo().getFilter() != null) {
            whereExpression = ODataExpressionParser.parseToJPAWhereExpression(blocklyQuery.getUriInfo().getFilter(), alias, ":crn_param_");
          }

          if (blocklyQuery.getUriInfo().getKeyPredicates().size() > 0) {
            whereExpression = ODataExpressionParser.parseKeyPredicates(blocklyQuery.getUriInfo().getKeyPredicates(), alias, ":crn_param_");
          }

          List<String> paramsWithName = whereExpression != null ? JPQLParserUtil.parseParams(whereExpression, ':') : null;

          if (paramsWithName != null && params != null) {
            int i = 0;
            info.params = new Var[queryParams.length + params.size()];
            for (Var v : queryParams) {
              info.params[i] = v;
              i++;
            }

            for (String p : params) {
              info.params[i] = Var.valueOf(paramsWithName.get(i), blocklyQuery.getParameterValue(p));
              i++;
            }
          }

        }

        String where = null;

        if (selectStatement != null && selectStatement.hasOrderByClause()) {
          orderBy = ((OrderByClause) selectStatement.getOrderByClause()).toString();
          ReflectionUtils.setField(selectStatement, "orderByClause", null);
          jpqlStatement = selectStatement.toString();
        }

        if (blocklyQuery.getUriInfo().getOrderBy() != null) {
          String orderExpression = ODataExpressionParser.parseToJPAOrderByExpression(blocklyQuery.getUriInfo().getOrderBy(), alias, ":crn_param_");
          orderBy = "ORDER BY " + orderExpression;
        }

        if (selectStatement != null && selectStatement.hasWhereClause()) {
          where = ((WhereClause) selectStatement.getWhereClause()).getConditionalExpression().toString();
          ReflectionUtils.setField(selectStatement, "whereClause", null);
          jpqlStatement = selectStatement.toString();
        }

        if (selectStatement != null && selectStatement.hasGroupByClause()) {
          groupBy = ((GroupByClause) selectStatement.getGroupByClause()).toString();
          ReflectionUtils.setField(selectStatement, "groupByClause", null);
          jpqlStatement = selectStatement.toString();
        }

        if (selectStatement != null && selectStatement.hasHavingClause()) {
          having = ((HavingClause) selectStatement.getHavingClause()).toString();
          ReflectionUtils.setField(selectStatement, "havingClause", null);
          jpqlStatement = selectStatement.toString();
        }

        if (whereExpression != null) {
          if (where != null) {
            jpqlStatement += " WHERE (" + where + ") AND " + whereExpression;
          } else {
            jpqlStatement += " WHERE " + whereExpression;
          }
        } else {
          if (where != null) {
            jpqlStatement += " WHERE " + where;
          }
        }

        if (having != null) {
          jpqlStatement += " " + having;
        }

        if (groupBy != null) {
          jpqlStatement += " " + groupBy;
        }

        if (orderBy != null) {
          jpqlStatement += " " + orderBy;
        }

        info.jpql = jpqlStatement;

        if (blocklyQuery.getFirstResult() != -1) {
          info.first = blocklyQuery.getFirstResult();
        }
        if (blocklyQuery.getMaxResults() != -1) {
          info.max = blocklyQuery.getMaxResults();
        }
        return info;
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  public static List<String> parseParams(String SQL) {
    return parseParams(SQL, ':');
  }

  public static List<String> parseParams(String SQL, char charDelim) {
    final String delims = " \n\r\t.(){},+=!" + charDelim;
    final String quots = "\'";
    String token = "";
    boolean isQuoted = false;
    List<String> tokens = new LinkedList<>();

    for (int i = 0; i < SQL.length(); i++) {
      if (quots.indexOf(SQL.charAt(i)) != -1) {
        isQuoted = token.length() == 0;
      }
      if (delims.indexOf(SQL.charAt(i)) == -1 || isQuoted) {
        token += SQL.charAt(i);
      } else {
        if (token.length() > 0) {
          if (token.startsWith("" + charDelim))
            tokens.add(token.substring(1));
          token = "";
          isQuoted = false;
        }
        if (SQL.charAt(i) == charDelim) {
          token = "" + charDelim;
        }
      }
    }

    if (token.length() > 0) {
      if (token.startsWith("" + charDelim))
        tokens.add(token.substring(1));
    }

    return tokens;
  }


}
