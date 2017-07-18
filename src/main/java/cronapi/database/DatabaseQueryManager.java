package cronapi.database;

import java.util.LinkedList;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import com.google.gson.JsonObject;

import cronapi.ErrorResponse;
import cronapi.QueryManager;
import cronapi.Var;

public class DatabaseQueryManager {

  private boolean isolatedTransaction = true;
  private String id;

  public DatabaseQueryManager(final String id) {
    this.id = id;
  }

  public DatabaseQueryManager(final String id, final boolean isolatedTransaction) {
    this.id = id;
    this.isolatedTransaction = isolatedTransaction;
  }

  public Var get(final Object ... params) throws Exception {
    return get(null, params);
  }

  private Var[] toVarArray(Object[] list) {
    Var[] vars = new Var[list.length];
    for(int i = 0; i < list.length; i++) {
      vars[i] = Var.valueOf(list[i]);
    }

    return vars;
  }

  public Var get(final PageRequest paramPage, final Object ... objParams) throws Exception {
    final PageRequest page = paramPage == null ? new PageRequest(0, 100) : paramPage;
    final Var[] params = toVarArray(objParams);
    return runIntoTransaction(() -> {
      
      JsonObject query = QueryManager.getQuery(id);
      QueryManager.checkSecurity(query, "GET");
      
      if(QueryManager.getType(query).equals("blockly")) {
        return QueryManager.executeBlockly(query, "GET", params);
      }
      else {
        DataSource ds = new DataSource(query.get("entityFullName").getAsString());
        String jpql = query.get("query") != null ? query.get("query").getAsString() : null;
        
        ds.filter(jpql, page, params);
        
        QueryManager.executeNavigateEvent(query, ds);
        
        return Var.valueOf(ds.getPage().getContent());
      }
    });
  }
  
  public Var insert(final Object objData, final Object ... extraObjParams) throws Exception {
    final Var[] extraParams = toVarArray(extraObjParams);
    final Var data = Var.valueOf(objData);

    return runIntoTransaction(() -> {
      
      JsonObject query = QueryManager.getQuery(id);
      QueryManager.checkSecurity(query, "POST");
      
      if(QueryManager.getType(query).equals("blockly")) {
        Var[] params = (Var[])ArrayUtils.addAll(new Var[] { data }, extraParams);
        QueryManager.executeEvent(query, data, "beforeInsert");
        Var inserted = QueryManager.executeBlockly(query, "POST", params);
        QueryManager.executeEvent(query, data, "afterInsert");
        
        return inserted;
      }
      else {
        DataSource ds = new DataSource(query.get("entityFullName").getAsString());
        
        ds.insert(data.getObject());
        
        QueryManager.addDefaultValues(query, ds);
        
        QueryManager.executeEvent(query, ds, "beforeInsert");
        Object inserted = ds.save(false);
        QueryManager.executeEvent(query, ds, "afterInsert");
        
        return Var.valueOf(inserted);
      }
    });
  }
  
  public Var update(final Var objData, final Object ... extraObjParams) throws Exception {
    final Var[] extraParams = toVarArray(extraObjParams);
    final Var data = Var.valueOf(objData);

    return runIntoTransaction(() -> {
      
      JsonObject query = QueryManager.getQuery(id);
      QueryManager.checkSecurity(query, "PUT");
      
      if(QueryManager.getType(query).equals("blockly")) {
        Var[] params = (Var[])ArrayUtils.addAll(new Var[] { data }, extraParams);
        QueryManager.executeEvent(query, data, "beforeUpdate");
        Var modified = QueryManager.executeBlockly(query, "PUT", params);
        QueryManager.executeEvent(query, data, "beforeUpdate");
        
        return modified;
      }
      else {
        DataSource ds = new DataSource(query.get("entityFullName").getAsString());
        
        ds.filter(data, null);
        QueryManager.executeEvent(query, ds, "beforeUpdate");
        ds.update(data);
        Var saved = Var.valueOf(ds.save());
        QueryManager.executeEvent(query, ds, "afterUpdate");
        return saved;
      }
    });
  }
  
  public void delete(final Object ... extraObjParams) throws Exception {
    final Var[] extraParams = toVarArray(extraObjParams);

    runIntoTransaction(() -> {
      
      JsonObject query = QueryManager.getQuery(id);
      QueryManager.checkSecurity(query, "DELETE");
      
      if(QueryManager.getType(query).equals("blockly")) {
        QueryManager.executeEvent(query, "beforeDelete", extraParams);
        QueryManager.executeBlockly(query, "DELETE", extraParams);
        QueryManager.executeEvent(query, "afterDelete", extraParams);
      }
      else {
        DataSource ds = new DataSource(query.get("entityFullName").getAsString());
        ds.filter(null, new PageRequest(1, 1), extraParams);
        QueryManager.executeEvent(query, ds, "beforeDelete");
        ds.delete();
        QueryManager.executeEvent(query, ds, "afterDelete");
      }
      return null;
    });
  }
  
  private Var runIntoTransaction(Callable<Var> callable) throws Exception {
    Var var = Var.VAR_NULL;
    try {
      var = callable.call();
      if (isolatedTransaction)
        TransactionManager.commit();
    }
    catch(Exception ex) {
      if (isolatedTransaction)
        TransactionManager.rollback();
      ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex, "GET");
      throw new Exception(errorResponse.getError(), ex);
    }
    return var;
  }
}
