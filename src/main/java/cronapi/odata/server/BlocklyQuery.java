package cronapi.odata.server;

import com.google.gson.JsonObject;
import cronapi.QueryManager;
import cronapi.Var;

import javax.persistence.*;
import java.util.*;

public class BlocklyQuery implements Query {

  private JsonObject query;
  private String method;
  private String queryStatement;
  private Map<String, Object> parameters = new LinkedHashMap<>();
  private String type;

  public BlocklyQuery(JsonObject query, String method, String type, String queryStatement, String originalFilter) {
    this.type = type;
    this.parameters.put("OriginalFilter", originalFilter);
    this.query = query;
    this.method = method;
    this.queryStatement = queryStatement;
  }

  @Override
  public List getResultList() {
    Var result = QueryManager.executeBlockly(query, this.method, Var.valueOf(type), Var.valueOf(this.queryStatement), Var.valueOf(parameters));

    if (query.get("baseEntity") != null) {
      try {
        parameters.put("baseEntity", query.get("baseEntity").getAsString());
        return (List) result.getObjectAsRawList(Class.forName(query.get("baseEntity").getAsString()));
      } catch(Exception e) {
        new RuntimeException(e);
      }
    }

    return result.getObjectAsList();
  }

  @Override
  public Object getSingleResult() {
    return getResultList().get(0);
  }

  @Override
  public int executeUpdate() {
    return 0;
  }

  @Override
  public Query setMaxResults(int maxResult) {
    parameters.put("MaxResults", maxResult);
    return this;
  }

  @Override
  public int getMaxResults() {
    return 0;
  }

  @Override
  public Query setFirstResult(int startPosition) {
    parameters.put("FirstResult", startPosition);
    return this;
  }

  @Override
  public int getFirstResult() {
    return 0;
  }

  @Override
  public Query setHint(String hintName, Object value) {
    parameters.put("hintName", value);
    return this;
  }

  @Override
  public Map<String, Object> getHints() {
    return null;
  }

  private void putParameter(int index, Object value) {
    parameters.put(String.valueOf(index), value);
  }

  @Override
  public <T> Query setParameter(Parameter<T> param, T value) {
    putParameter(param.getPosition(), value);
    return this;
  }

  @Override
  public Query setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType) {
    putParameter(param.getPosition(), value);
    return this;
  }

  @Override
  public Query setParameter(Parameter<Date> param, Date value, TemporalType temporalType) {
    putParameter(param.getPosition(), value);
    return this;
  }

  @Override
  public Query setParameter(String name, Object value) {
    parameters.put(name, value);
    return this;
  }

  @Override
  public Query setParameter(String name, Calendar value, TemporalType temporalType) {
    parameters.put(name, value);
    return this;
  }

  @Override
  public Query setParameter(String name, Date value, TemporalType temporalType) {
    parameters.put(name, value);
    return this;
  }

  @Override
  public Query setParameter(int position, Object value) {
    putParameter(position, value);
    return this;
  }

  @Override
  public Query setParameter(int position, Calendar value, TemporalType temporalType) {
    putParameter(position, value);
    return this;
  }

  @Override
  public Query setParameter(int position, Date value, TemporalType temporalType) {
    putParameter(position, value);
    return this;
  }

  @Override
  public Set<Parameter<?>> getParameters() {
    return null;
  }

  @Override
  public Parameter<?> getParameter(String name) {
    return null;
  }

  @Override
  public <T> Parameter<T> getParameter(String name, Class<T> type) {
    return null;
  }

  @Override
  public Parameter<?> getParameter(int position) {
    return null;
  }

  @Override
  public <T> Parameter<T> getParameter(int position, Class<T> type) {
    return null;
  }

  @Override
  public boolean isBound(Parameter<?> param) {
    return false;
  }

  @Override
  public <T> T getParameterValue(Parameter<T> param) {
    return null;
  }

  @Override
  public Object getParameterValue(String name) {
    return null;
  }

  @Override
  public Object getParameterValue(int position) {
    return null;
  }

  @Override
  public Query setFlushMode(FlushModeType flushMode) {
    return null;
  }

  @Override
  public FlushModeType getFlushMode() {
    return null;
  }

  @Override
  public Query setLockMode(LockModeType lockMode) {
    return null;
  }

  @Override
  public LockModeType getLockMode() {
    return null;
  }

  @Override
  public <T> T unwrap(Class<T> cls) {
    return null;
  }
}
