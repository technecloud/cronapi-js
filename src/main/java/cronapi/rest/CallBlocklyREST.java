package cronapi.rest;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import cronapi.ErrorResponse;
import cronapi.RestBody;
import cronapi.RestClient;
import cronapi.RestResult;
import cronapi.Var;
import cronapi.database.DataSource;
import cronapi.database.TransactionManager;

@RestController
@RequestMapping(value = "/api/cronapi")
public class CallBlocklyREST {

  @Autowired
  private HttpServletRequest request;

  private Var[] translatePathVars(String clazz) {
    String paths = request.getServletPath().substring(request.getServletPath().indexOf(clazz) + clazz.length());
    if (paths.startsWith("/")) {
      paths = paths.substring(1);
    }
    if (paths.endsWith("/")) {
      paths = paths.substring(0, paths.length()-1);
    }

    String[] strParams = paths.split("/");
    Var[] vars = new Var[strParams.length];
    for (int i=0; i<strParams.length;i++) {
      vars[i] = new Var(strParams[i]);
    }

    return vars;
  }
  
  @ExceptionHandler(Throwable.class)
  @ResponseBody
  ResponseEntity<ErrorResponse> handleControllerException(HttpServletRequest req, Throwable ex) {
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex);
    return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
  
  @RequestMapping(method = RequestMethod.GET, value = "/query/{entity}/{id}/**")
  public HttpEntity<List> queryGet(@PathVariable("entity") String entity, @PathVariable("id") String id, Pageable pageable) throws Exception {
    RestResult data = runIntoTransaction(() -> {
      Var[] params = translatePathVars(id);
      String query = null;

      DataSource ds = new DataSource(entity);
      PageRequest page = new PageRequest(pageable.getPageNumber(), pageable.getPageSize());
      ds.filter(query, page, params);

      return Var.valueOf(ds.getPage());
    });

    Page page = (Page) data.getValue().getObject();

    return new ResponseEntity<List>(page.getContent(), HttpStatus.OK);
  }

  @RequestMapping(method = RequestMethod.POST, value = "/query/{entity}/{id}/**")
  public HttpEntity<Object> queryPost(@PathVariable("entity") String entity, @PathVariable("id") String id, @RequestBody final Var data) throws Exception {
    RestResult result = runIntoTransaction(() -> {
      DataSource ds = new DataSource(entity);
      ds.filter(data);
      ds.update(data);
      return Var.valueOf(ds.save());
    });

    return new ResponseEntity<Object>(result.getValue().getObject(), HttpStatus.OK);
  }

  @RequestMapping(method = RequestMethod.PUT, value = "/query/{entity}/{id}")
  public HttpEntity<Object> queryPut(@PathVariable("entity") String entity, @PathVariable("id") String id, @RequestBody final Var data) throws Exception {
    RestResult result = runIntoTransaction(() -> {
      DataSource ds = new DataSource(entity);
      ds.insert((Map<?,?>) data.getObject());
      return Var.valueOf(ds.save());
    });

    return new ResponseEntity<Object>(result.getValue().getObject(), HttpStatus.OK);
  }

  @RequestMapping(method = RequestMethod.DELETE, value = "/query/{entity}/{id}/**")
  public void queryDelete(@PathVariable("entity") String entity, @PathVariable("id") String id) throws Exception {
    runIntoTransaction(() -> {
      Var[] vars = translatePathVars(id);
      DataSource ds = new DataSource(entity);
      ds.delete(vars);
      return null;
    });
  }

  @RequestMapping(method = RequestMethod.POST, value = "/call/body/{class}")
  public RestResult postBody(@RequestBody RestBody body, @PathVariable("class") String clazz) throws Exception {
    return runIntoTransaction(() -> {
      RestClient.getRestClient().setBody(body);
      return cronapi.util.Operations.callBlockly(new Var(clazz), body.getInputs());
    });
  }

  @RequestMapping(method = RequestMethod.GET, value = "/call/{class}/**")
  public RestResult getOneParam(@PathVariable("class") String clazz)
          throws Exception {
    return runIntoTransaction(() -> {
      Var[] vars = translatePathVars(clazz);
      return cronapi.util.Operations.callBlockly(new Var(clazz), vars);
    });
  }

  @RequestMapping(method = RequestMethod.POST, value = "/call/{class}")
  public RestResult postParams(@RequestBody Var[] vars, @PathVariable("class") String clazz) throws Exception {
    return runIntoTransaction(() -> {
      return cronapi.util.Operations.callBlockly(new Var(clazz), vars);
    });
  }
  
  private RestResult runIntoTransaction(Callable<Var> callable) throws Exception {
    Var var = Var.VAR_NULL;
    try {
      var = callable.call();
      TransactionManager.commit();
    }
    catch(Exception e) {
      TransactionManager.rollback();
      throw e;
    }
    finally {
      TransactionManager.close();
      TransactionManager.clear();
    }
    return new RestResult(var, RestClient.getRestClient().getCommands());
  }
}
