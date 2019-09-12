package cronapi.odata.server;

import cronapi.util.ReflectionUtils;
import org.eclipse.persistence.jpa.jpql.parser.*;

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

  public static String getCountJPQL(String jpql) {
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

    ReflectionUtils.getField(selectStatement, "selectClause");

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

    return selectExpression;

  }
}
