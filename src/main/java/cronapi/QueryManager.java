package cronapi;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

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
      for (Map.Entry<String, JsonElement> entry : getJSON().entrySet()) {
        JsonObject customObj = entry.getValue().getAsJsonObject();
        if (!isNull(customObj.get("customId")) && customObj.get("customId").getAsString().equalsIgnoreCase(id)) {
          obj = customObj;
          break;
        }
      }
      if(obj == null) {
        throw new RuntimeException(Messages.getString("queryNotFound"));
      }
    }

    RestClient.getRestClient().setQuery(obj);
    return obj;
  }

  public static String getType(JsonObject obj) {
    if (obj.get("sourceType") != null && !obj.get("sourceType").isJsonNull()) {
      return obj.get("sourceType").getAsString();
    }

    return "entityFullName";
  }

  public static void checkSecurity(JsonObject obj, String verb) {
    if(!obj.getAsJsonObject("verbs").get(verb).getAsBoolean()) {
      throw new RuntimeException(Messages.format(Messages.getString("verbNotAllowed"), verb));
    }
  }

  private static boolean isNull(JsonElement value) {
    return value == null || value.isJsonNull();
  }

  public static void addDefaultValues(JsonObject query, DataSource ds) {
    if(!isNull(query.get("defaultValues"))) {
      for(Map.Entry<String, JsonElement> entry : query.get("defaultValues").getAsJsonObject().entrySet()) {
        if(!entry.getValue().isJsonNull()) {
          Var value;
          if (entry.getValue().isJsonObject()) {
            JsonObject event = entry.getValue().getAsJsonObject();
            Var name = Var.valueOf(event.get("blocklyClass").getAsString() + ":" + event.get("blocklyMethod").getAsString());
            try {
              value = Operations.callBlockly(name, Var.valueOf(ds));
            }
            catch(Exception e) {
              throw new RuntimeException(e);
            }
          } else {
            value = Var.valueOf(entry.getValue().getAsString());
          }

          ds.updateField(entry.getKey(), value);
        }
      }
    }
  }

  public static void executeEvent(JsonObject query, Object ds, String eventName) {
    JsonObject events = query.getAsJsonObject("events");
    if(!isNull(events)) {
      if(!isNull(events.get(eventName))) {
        JsonObject event = events.getAsJsonObject(eventName);
        Var name = Var.valueOf(event.get("blocklyClass").getAsString() + ":" + event.get("blocklyMethod").getAsString());
        try {
          Operations.callBlockly(name, Var.valueOf(ds));
        } catch(Exception e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  public static void executeEvent(JsonObject query, String eventName, Var...params) {
    JsonObject events = query.getAsJsonObject("events");
    if(!isNull(events)) {
      if(!isNull(events.get(eventName))) {
        JsonObject event = events.getAsJsonObject(eventName);
        Var name = Var.valueOf(event.get("blocklyClass").getAsString() + ":" + event.get("blocklyMethod").getAsString());
        try {
          Operations.callBlockly(name, params);
        } catch(Exception e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  public static void executeNavigateEvent(JsonObject query, DataSource ds) {
    JsonObject events = query.getAsJsonObject("events");
    if(!isNull(events)) {
      if(!isNull(events.get("onNavigate"))) {
        JsonObject event = events.getAsJsonObject("onNavigate");
        
        Var name = Var.valueOf(event.get("blocklyClass").getAsString() + ":" + event.get("blocklyMethod").getAsString());
        Var dsVar = Var.valueOf(ds);
        
        int current = ds.getCurrent();
        int size = ds.getPage().getContent().size();
        for(int i = 0; i < size; i++) {
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

  private static Var doExecuteBlockly(JsonObject blockly, String method, Var...params) throws Exception {
    String function =  blockly.get("blocklyMethod").getAsString();

    if (!isNull(blockly.get("blockly"+method+"Method"))) {
      function = blockly.get("blockly"+method+"Method").getAsString();
    }

    Var name = Var.valueOf(blockly.get("blocklyClass").getAsString() + ":" + function);
    return Operations.callBlockly(name, params);
  }

  public static Var executeBlockly(JsonObject query, String method, Var...vars) {
    if(!isNull(query.getAsJsonObject("blockly"))) {
      try {
        return doExecuteBlockly(query.getAsJsonObject("blockly"), method, vars);
      }
      catch(Exception e) {
        throw new RuntimeException(e);
      }
    }

    return Var.VAR_NULL;
  }


}
