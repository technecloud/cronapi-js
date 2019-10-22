package cronapi.odata.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cronapi.QueryManager;
import cronapi.RestClient;
import cronapi.Var;
import org.apache.olingo.odata2.api.uri.UriInfo;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TemporalType;

public class BlocklyQuery implements Query {

  public static ThreadLocal<BlocklyQuery> CURRENT_BLOCK_QUERY = new ThreadLocal<>();

  private JsonObject query;
  private String method;
  private String queryStatement;
  private Map<String, Object> parameters = new LinkedHashMap<>();
  private String type;
  private String originalFilter;
  private String entityName;
  private Var lastResult;
  private UriInfo uriInfo;

  public BlocklyQuery(JsonObject query, String method, String type, String queryStatement, String originalFilter, String entityName) {
    this.type = type;
    this.originalFilter = originalFilter;
    this.entityName = entityName;
    this.parameters.put("queryType", type);
    this.parameters.put("queryStatement", queryStatement);
    this.parameters.put("queryFilter", originalFilter);
    this.query = query;
    this.method = method;
    this.queryStatement = queryStatement;
  }

  public static boolean isNull(JsonElement value) {
    return value == null || value.isJsonNull();
  }

  @Override
  public List getResultList() {

    try {
      CURRENT_BLOCK_QUERY.set(this);

      Var result = null;

      if (lastResult != null) {
        result = lastResult;
      }

      if (result == null) {
        Var[] params = new Var[0];

        if (!isNull(query.get("queryParamsValues"))) {
          JsonArray paramValues = query.getAsJsonArray("queryParamsValues");
          params = new Var[paramValues.size()];
          for (int x = 0; x < paramValues.size(); x++) {
            JsonObject prv = paramValues.get(x).getAsJsonObject();
            if (!isNull(prv.get("fieldName"))) {
              String name = prv.get("fieldName").getAsString();
              params[x] = Var.VAR_NULL;
              if (!isNull(prv.get("fieldValue"))) {

                Map<String, Var> customValues = new LinkedHashMap<>();
                customValues.put("entityName", Var.valueOf(this.entityName));

                customValues.put("queryType", Var.valueOf(type));
                customValues.put("queryStatement", Var.valueOf(queryStatement));
                customValues.put("queryFilter", Var.valueOf(originalFilter));
                customValues.put("queryParameters", Var.valueOf(parameters));

                String strValue = RestClient.getRestClient().getParameter(name);
                if (strValue != null) {
                  params[x] = Var.valueOf(strValue);
                } else {
                  params[x] = QueryManager.getParameterValue(query, name, customValues);
                }
              }
            }
          }
        }

        result = QueryManager.executeBlockly(query, this.method, params);
      }

      if (!QueryManager.isNull(query.get("baseEntity"))) {
        try {
          parameters.put("baseEntity", query.get("baseEntity").getAsString());
          lastResult = result;
          return (List) result.getObjectAsRawList(LinkedList.class);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }

      lastResult = result;
      return result.getObjectAsList();
    } finally {
      CURRENT_BLOCK_QUERY.remove();
    }
  }

  public void setUriInfo(UriInfo uriInfo) {
    this.uriInfo = uriInfo;
  }

  public Var getLastResult() {
    return lastResult;
  }

  public void setLastResult(Var value) {
    this.lastResult = value;
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
    return parameters.containsKey("MaxResults") ? (int) parameters.get("MaxResults") : -1;
  }

  @Override
  public Query setFirstResult(int startPosition) {
    parameters.put("FirstResult", startPosition);
    return this;
  }

  @Override
  public int getFirstResult() {
    return parameters.containsKey("FirstResult") ? (int) parameters.get("FirstResult") : -1;
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
    return parameters.get(name);
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

  public UriInfo getUriInfo() {
    return uriInfo;
  }
}
