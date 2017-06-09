package cronapi.rest;

import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;

import cronapi.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cronapi.database.TransactionManager;

@RestController
@RequestMapping(value = "/api/cronapi/call")
public class CallBlocklyREST {
  
  @ExceptionHandler(Throwable.class)
  @ResponseBody
  ResponseEntity<ErrorResponse> handleControllerException(HttpServletRequest req, Throwable ex) {
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex);
    return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
  
  @RequestMapping(method = RequestMethod.POST, value = "/body/{class}")
  public RestResult postBody(@RequestBody RestBody body, @PathVariable("class") String clazz) throws Exception {
    return runIntoTransaction(() -> {
      RestClient.getRestClient().setBody(body);
      return cronapi.util.Operations.callBlockly(new Var(clazz), body.getInputs());
    });
  }
  
  @RequestMapping(method = RequestMethod.GET, value = "/{class}")
  public RestResult getNoParam(@PathVariable("class") String clazz) throws Exception {
    return runIntoTransaction(() -> {
      return cronapi.util.Operations.callBlockly(new Var(clazz));
    });
  }
  
  @RequestMapping(method = RequestMethod.GET, value = "/{class}/{param1}")
  public RestResult getOneParam(@PathVariable("class") String clazz, @PathVariable("param1") String param1)
          throws Exception {
    return runIntoTransaction(() -> {
      return cronapi.util.Operations.callBlockly(new Var(clazz), new Var(param1));
    });
  }
  
  @RequestMapping(method = RequestMethod.GET, value = "/{class}/{param1}/{param2}")
  public RestResult getTwoParams(@PathVariable("class") String clazz, @PathVariable("param1") String param1,
                                 @PathVariable("param2") String param2)
          throws Exception {
    
    return runIntoTransaction(() -> {
      return cronapi.util.Operations.callBlockly(new Var(clazz), new Var(param1), new Var(param2));
    });
  }
  
  @RequestMapping(method = RequestMethod.GET, value = "/{class}/{param1}/{param2}/{param3}")
  public RestResult getThreeParams(@PathVariable("class") String clazz, @PathVariable("param1") String param1,
                                   @PathVariable("param2") String param2, @PathVariable("param3") String param3)
          throws Exception {
    return runIntoTransaction(() -> {
      return cronapi.util.Operations.callBlockly(new Var(clazz), new Var(param1), new Var(param2), new Var(param3));
    });
  }
  
  @RequestMapping(method = RequestMethod.POST, value = "/{class}")
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
