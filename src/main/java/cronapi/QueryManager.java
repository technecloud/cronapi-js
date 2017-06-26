package cronapi;

import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cronapi.database.DataSource;
import cronapi.i18n.Messages;
import cronapi.util.Operations;

public class QueryManager {
  private static JsonObject JSON;
  
  static {
    JSON = loadJSON();
  }
  
  private static JsonObject loadJSON() {
    ClassLoader classLoader = QueryManager.class.getClassLoader();
    try (InputStream stream = classLoader.getResourceAsStream("META-INF/customQuery.json")) {
      InputStreamReader reader = new InputStreamReader(stream);
      JsonElement jsonElement = new JsonParser().parse(reader);
      return jsonElement.getAsJsonObject();
    }
    catch(Exception e) {
      return new JsonObject();
    }
  }
  
  private static JsonObject getJSON() {
    if(Operations.IS_DEBUG) {
      return loadJSON();
    }
    else {
      return JSON;
    }
  }
  
  public static JsonObject getQuery(String id) {
    JsonObject obj = getJSON().getAsJsonObject(id);
    if(obj == null) {
      throw new RuntimeException(Messages.getString("queryNotFound"));
    }
    
    return obj;
  }
  
  public static void checkSecurity(JsonObject obj, String verb) {
    if(!obj.getAsJsonObject("verbs").get(verb).getAsBoolean()) {
      throw new RuntimeException(String.format(Messages.getString("verbNotAllowed"), verb));
    }
  }
  
  public static void executeEvent(JsonObject obj, DataSource ds, String eventName) {
    if(obj.getAsJsonObject("events").getAsJsonObject(eventName) != null) {
      JsonObject event = obj.getAsJsonObject("events").getAsJsonObject(eventName);
      Var name = Var.valueOf(event.get("blocklyClass").getAsString() + ":" + event.get("blocklyMethod").getAsString());
      try {
        Operations.callBlockly(name, Var.valueOf(ds));
      }
      catch(Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
  
  public static void executeNavigateEvent(JsonObject obj, DataSource ds) {
    if(obj.getAsJsonObject("events").getAsJsonObject("onNavigate") != null) {
      JsonObject event = obj.getAsJsonObject("events").getAsJsonObject("onNavigate");
      Var name = Var.valueOf(event.get("blocklyClass").getAsString() + ":" + event.get("blocklyMethod").getAsString());

      Var dsVar = Var.valueOf(ds);

      int current = ds.getCurrent();

      for (int i=0;i<ds.getPage().getContent().size();i++) {
        try {
          Operations.callBlockly(name, dsVar);
          ds.nextOnPage();
        }
        catch(Exception e) {
          throw new RuntimeException(e);
        }
      }

      ds.setCurrent(current);
    }
    
  }
}
