package cronapi.odata.server;

import cronapi.util.ReflectionUtils;
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
}
