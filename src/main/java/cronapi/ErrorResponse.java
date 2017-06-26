package cronapi;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cronapi.i18n.Messages;
import cronapi.util.Operations;

public class ErrorResponse {
  private static JsonObject DATABASE;
  private static HashSet<String> IGNORED = new HashSet<>();
  
  private String error;
  private int status;
  
  private String stackTrace;
  
  static {
    IGNORED.add("java.lang.reflect.InvocationTargetException");
    IGNORED.add("java.lang.NullPointerException");
    DATABASE = loadJSON();
  }
  
  private static JsonObject loadJSON() {
    ClassLoader classLoader = QueryManager.class.getClassLoader();
    try (InputStream stream = classLoader.getResourceAsStream("cronapi/database/databases.json")) {
      InputStreamReader reader = new InputStreamReader(stream);
      JsonElement jsonElement = new JsonParser().parse(reader);
      return jsonElement.getAsJsonObject();
    }
    catch(Exception e) {
      return new JsonObject();
    }
  }
  
  private static JsonObject getDataBaseJSON() {
    if(Operations.IS_DEBUG) {
      return loadJSON();
    }
    else {
      return DATABASE;
    }
  }
  
  private static String heandleDatabaseException(String message) {
    for(JsonElement elem : getDataBaseJSON().getAsJsonArray("primaryKeyError")) {
      if(message.toLowerCase().contains(elem.getAsString().toLowerCase())) {
        return "primaryKeyError";
      }
    }
    
    for(JsonElement elem : getDataBaseJSON().getAsJsonArray("foreignKeyError")) {
      if(message.toLowerCase().contains(elem.getAsString().toLowerCase())) {
        return "foreignKeyError";
      }
    }
    
    return message;
  }
  
  public ErrorResponse(int status, Throwable ex) {
    this.error = getExceptionMessage(ex);
    this.status = status;
    
    if(ex != null) {
      StringWriter writer = new StringWriter();
      ex.printStackTrace(new PrintWriter(writer));
      
      this.stackTrace = writer.toString();
    }
  }
  
  public String getError() {
    return error;
  }
  
  public void setError(String error) {
    this.error = error;
  }
  
  public int getStatus() {
    return status;
  }
  
  public void setStatus(int status) {
    this.status = status;
  }
  
  public String getStackTrace() {
    return stackTrace;
  }
  
  public void setStackTrace(String stackTrace) {
    this.stackTrace = stackTrace;
  }
  
  private static boolean hasThrowable(Throwable ex, Class clazz) {
    while(ex != null) {
      if(ex.getClass() == clazz) {
        return true;
      }
      
      ex = ex.getCause();
    }
    
    return false;
  }
  
  public static String getExceptionMessage(Throwable ex) {
    
    String message = null;
    
    if(ex != null) {
      if(ex.getMessage() != null && !ex.getMessage().trim().isEmpty() && !IGNORED.contains(ex.getMessage().trim())) {
        message = ex.getMessage();
        if(hasThrowable(ex, javax.persistence.RollbackException.class) ||
                hasThrowable(ex, javax.persistence.PersistenceException.class)) {
          message = heandleDatabaseException(message);
        }
      }
      else {
        if(ex.getCause() != null) {
          return getExceptionMessage(ex.getCause());
        }
      }
    }
    
    if(message == null) {
      return Messages.getString("errorNotSpecified");
    }
    
    return message;
    
  }
}
