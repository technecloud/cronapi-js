package cronapi.rest;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import cronapi.*;
import cronapi.database.DataSource;
import cronapi.database.EntityMetadata;
import cronapi.database.Operations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cronapi.database.TransactionManager;

@RestController
@RequestMapping(value = "/api/cronapi")
public class CallBlocklyREST {
  
  private static Pattern RELATION_PARAM = Pattern.compile("relation:(.*?):(.*?)$");
  
  @Autowired
  private HttpServletRequest request;
  
  private class TranslationPath {
    Var[] params;
    String relationClass;
    String relationAssossiative;
    String field;
    String refId;
    Var[] relationParams;
  }
  
  private TranslationPath translatePathVars(String clazz) {
    String paths = request.getServletPath().substring(request.getServletPath().indexOf(clazz) + clazz.length()).trim();
    if(paths.startsWith("/")) {
      paths = paths.substring(1);
    }
    if(paths.endsWith("/")) {
      paths = paths.substring(0, paths.length() - 1);
    }
    
    String[] strParams = paths.split("/");
    List<Var> params = new LinkedList<>();
    List<Var> relationParams = new LinkedList<>();
    
    TranslationPath translationPath = new TranslationPath();
    
    boolean isParam = true;
    boolean isRelationParam = false;
    if(!paths.isEmpty()) {
      for(int i = 0; i < strParams.length; i++) {
        
        Matcher matcher = RELATION_PARAM.matcher(strParams[i]);
        if(matcher.matches()) {
          translationPath.relationClass = matcher.group(2);
          translationPath.field = matcher.group(1);
          translationPath.refId = strParams[i];
          isParam = false;
          isRelationParam = true;
          continue;
        }
        
        if(isParam) {
          params.add(Var.valueOf(strParams[i]));
        }
        
        if(isRelationParam) {
          relationParams.add(Var.valueOf(strParams[i]));
        }
      }
    }
    translationPath.params = params.toArray(new Var[params.size()]);
    translationPath.relationParams = relationParams.toArray(new Var[relationParams.size()]);
    
    return translationPath;
  }
  
  private Var[] toVarArray(LinkedList list) {
    Var[] vars = new Var[list.size()];
    for(int i = 0; i < list.size(); i++) {
      vars[i] = Var.valueOf(list.get(i));
    }
    
    return vars;
  }
  
  @ExceptionHandler(Throwable.class)
  @ResponseBody
  ResponseEntity<ErrorResponse> handleControllerException(HttpServletRequest req, Throwable ex) {
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex);
    return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
  
  @RequestMapping(method = RequestMethod.GET, value = "/metadata/{entity}/**")
  public HttpEntity<EntityMetadata> dataOptions(@PathVariable("entity") String entity) throws Exception {
    DataSource ds = new DataSource(entity);
    EntityMetadata data = ds.getMetadata();
    return new ResponseEntity<EntityMetadata>(data, HttpStatus.OK);
  }
  
  @RequestMapping(method = RequestMethod.GET, value = "/crud/{entity}/**")
  public HttpEntity<List> dataGet2(@PathVariable("entity") String entity, Pageable pageable) throws Exception {
    RestResult data = runIntoTransaction(() -> {
      
      TranslationPath translationPath = translatePathVars(entity);
      
      PageRequest page = new PageRequest(pageable.getPageNumber(), pageable.getPageSize());
      
      DataSource ds = new DataSource(entity);
      if(translationPath.relationClass == null) {
        ds.filter(null, page, translationPath.params);
      }
      else {
        ds.filterByRelation(translationPath.refId, page, translationPath.params);
      }
      
      return Var.valueOf(ds.getPage());
    });
    
    Page page = (Page)data.getValue().getObject();
    
    return new ResponseEntity<List>(page.getContent(), HttpStatus.OK);
  }
  
  @RequestMapping(method = RequestMethod.PUT, value = "/crud/{entity}/**")
  public HttpEntity<Object> dataPost(@PathVariable("entity") String entity, @RequestBody final Var data)
          throws Exception {
    RestResult result = runIntoTransaction(() -> {
      DataSource ds = new DataSource(entity);
      ds.filter(data);
      ds.update(data);
      return Var.valueOf(ds.save());
    });
    
    return new ResponseEntity<Object>(result.getValue().getObject(), HttpStatus.OK);
  }
  
  @RequestMapping(method = RequestMethod.POST, value = "/crud/{entity}/**")
  public HttpEntity<Object> dataPut(@PathVariable("entity") String entity, @RequestBody final Var data)
          throws Exception {
    RestResult result = runIntoTransaction(() -> {
      DataSource ds = new DataSource(entity);
      TranslationPath translationPath = translatePathVars(entity);

      Object inserted = null;
      if(translationPath.relationClass == null) {
        ds.insert((Map<?, ?>)data.getObject());
        inserted = ds.save();
      } else {
        inserted = ds.insertRelation(translationPath.refId, (Map<?, ?>)data.getObject(), translationPath.params);
      }
      return Var.valueOf(inserted);
    });
    
    return new ResponseEntity<Object>(result.getValue().getObject(), HttpStatus.OK);
  }
  
  @RequestMapping(method = RequestMethod.DELETE, value = "/crud/{entity}/**")
  public void dataDelete(@PathVariable("entity") String entity) throws Exception {
    runIntoTransaction(() -> {
      TranslationPath translationPath = translatePathVars(entity);
      DataSource ds = new DataSource(entity);
      if (translationPath.relationClass == null) {
        ds.delete(translationPath.params);
      } else {
        ds.deleteRelation(translationPath.refId, translationPath.params, translationPath.relationParams);
      }
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
  public RestResult getOneParam(@PathVariable("class") String clazz) throws Exception {
    return runIntoTransaction(() -> {
      TranslationPath translationPath = translatePathVars(clazz);
      return cronapi.util.Operations.callBlockly(new Var(clazz), translationPath.params);
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
