package cronapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cronapi.util.Operations;

import java.io.InputStream;
import java.io.InputStreamReader;

public class AppConfig {

  public static boolean FORCE_METADATA = false;
  public static boolean FORCE_LOCAL_ENTITIES = false;
  private static JsonObject JSON;

  static {
    JSON = loadJSON();
  }

  private static JsonObject loadJSON() {
    ClassLoader classLoader = QueryManager.class.getClassLoader();
    try (InputStream stream = classLoader.getResourceAsStream("META-INF/app.config")) {
      InputStreamReader reader = new InputStreamReader(stream);
      JsonElement jsonElement = new JsonParser().parse(reader);
      return jsonElement.getAsJsonObject();
    } catch (Exception e) {
      return new JsonObject();
    }
  }

  public static JsonObject getJSON() {
    if (Operations.IS_DEBUG) {
      return loadJSON();
    } else {
      return JSON;
    }
  }

  public static boolean isNull(JsonElement value) {
    return value == null || value.isJsonNull();
  }

  public static boolean exposeLocalEntities() {
    JsonObject config = getJSON();
    if (!isNull(config.get("odata"))) {
      JsonElement elem = config.get("odata").getAsJsonObject().get("exposeEntities");
      return (!isNull(elem) && elem.getAsBoolean()) || FORCE_LOCAL_ENTITIES;
    }

    return true;
  }

  public static boolean exposeMetadada() {
    JsonObject config = getJSON();
    if (!isNull(config.get("odata"))) {
      JsonElement elem = config.get("odata").getAsJsonObject().get("exposeMetadata");
      return (!isNull(elem) && elem.getAsBoolean()) || FORCE_METADATA;
    }

    return true;
  }

  public static String exposeMetadadaSecurity() {
    JsonObject config = getJSON();
    if (!isNull(config.get("odata"))) {
      JsonElement elem = config.get("odata").getAsJsonObject().get("exposeMetadadaSecurity");
      return !isNull(elem) ? elem.getAsString() : null;
    }

    return null;
  }

  public static String exposeEnitiesSecurity() {
    JsonObject config = getJSON();
    if (!isNull(config.get("odata"))) {
      JsonElement elem = config.get("odata").getAsJsonObject().get("exposeEnitiesSecurity");
      return !isNull(elem) ? elem.getAsString() : null;
    }

    return null;
  }

  public static String tokenRecaptcha() {
    JsonObject config = getJSON();
    if (!isNull(config.get("security")) && config.get("security").getAsJsonObject().has("tokenRecaptcha")) {
      JsonElement elem = config.get("security").getAsJsonObject().get("tokenRecaptcha");
      if (!isNull(elem)) {
        return elem.getAsString();
      }
    }
    return "";
  }

  //TODO: CRONAPP-1208 - Remover Depois - JIRA DE REMOÇÃO CRONAPP-1220
  public static String tokenSeleniumIdeId(){
    JsonObject config = getJSON();
    if (!isNull(config.get("security"))) {
      JsonElement elem = config.get("security").getAsJsonObject().get("tokenSeleniumIdeId");
      if (!isNull(elem)) {
        return elem.getAsString();
      }
    }
    return "";
  }

  //TODO: CRONAPP-1208 - Remover Depois - JIRA DE REMOÇÃO CRONAPP-1220
  public static String tokenCronappId(){
    JsonObject config = getJSON();
    if (!isNull(config.get("security"))) {
      JsonElement elem = config.get("security").getAsJsonObject().get("tokenCronappId");
      if (!isNull(elem)) {
        return elem.getAsString();
      }
    }
    return "";
  }

  public static String token() {
    JsonObject config = getJSON();
    if (!isNull(config.get("security"))) {
      JsonElement elem = config.get("security").getAsJsonObject().get("token");
      if (!isNull(elem)) {
        return elem.getAsString();
      }
    }

    return "9SyECk96oDsTmXfogIieDI0cD/8FpnojlYSUJT5U9I/FGVmBz5oskmjOR8cbXTvoPjX+Pq/T/b1PqpHX0lYm0oCBjXWICA==";
  }

  public static String guid() {
    JsonObject config = getJSON();
    if (!isNull(config.get("app"))) {
      JsonElement elem = config.get("app").getAsJsonObject().get("guid");
      if (!isNull(elem)) {
        return elem.getAsString();
      }
    }

    return "00000000-0000-0000-0000-000000000000";
  }

  public static long tokenExpiration() {
    JsonObject config = getJSON();
    if (!isNull(config.get("security"))) {
      JsonElement elem = config.get("security").getAsJsonObject().get("tokenExpiration");
      if (!isNull(elem)) {
        return elem.getAsLong();
      }
    }

    return 3600L;
  }

  public static String type() {
    JsonObject config = loadJSON();
    if (!isNull(config.get("auth"))) {
      JsonElement elem = config.get("auth").getAsJsonObject().get("type");
      if (!isNull(elem)) {
        return elem.getAsString();
      }
    }

    return null;
  }

  public static String defaultDomain() {
    JsonObject config = loadJSON();
    if (!isNull(config.get("auth"))) {
      JsonElement elem = config.get("auth").getAsJsonObject().get("defaultDomain");
      if (!isNull(elem)) {
        return elem.getAsString();
      }
    }

    return null;
  }

  public static String hostname() {
    JsonObject config = loadJSON();
    if (!isNull(config.get("auth"))) {
      JsonElement elem = config.get("auth").getAsJsonObject().get("hostname");
      if (!isNull(elem)) {
        return elem.getAsString();
      }
    }

    return null;
  }

  public static boolean autoSignUp() {
    JsonObject config = loadJSON();
    if (!isNull(config.get("auth"))) {
      JsonElement elem = config.get("auth").getAsJsonObject().get("autoSignUp");
      if (!isNull(elem)) {
        return elem.getAsBoolean();
      }
    }

    return false;
  }

  public static boolean fixedTimeZone() {
    JsonObject config = loadJSON();
    if (!isNull(config.get("fixedTimeZone"))) {
      return config.get("fixedTimeZone").getAsBoolean();
    }

    return true;
  }

  public static String timeZone() {
    JsonObject config = loadJSON();
    if (!isNull(config.get("timeZone"))) {
      return config.get("timeZone").getAsString();
    }

    return "UTC";
  }

  public static int timeZoneOffset() {
    JsonObject config = loadJSON();
    if (!isNull(config.get("timeZoneOffset"))) {
      return config.get("timeZoneOffset").getAsInt();
    }

    return 0;
  }
}
