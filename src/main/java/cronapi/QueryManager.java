package cronapi;

import com.google.gson.*;
import cronapi.util.Operations;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
    return getJSON().getAsJsonObject(id);
  }
}
