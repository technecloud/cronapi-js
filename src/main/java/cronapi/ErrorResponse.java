package cronapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cronapi.i18n.AppMessages;
import cronapi.i18n.Messages;
import cronapi.util.Operations;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.persistence.exceptions.DatabaseException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ErrorResponse {
  private static final Pattern EXCEPTION_NAME_PATTERN = Pattern.compile("^([a-zA-Z0-9]+\\.[a-zA-Z0-9._]+:)");

  private static JsonObject DATABASE;
  private static HashSet<String> IGNORED = new HashSet<>();

  private String error;
  private int status;
  private String method;

  private String stackTrace;

  private static final String PRIMARY_KEY = "primaryKey";
  private static final String PRIMARY_KEY_ERROR = "primaryKeyError";
  private static final String FOREIGN_KEY = "foreignKey";
  private static final String FOREIGN_KEY_ERROR = "foreignKeyError";
  private static final String OPTIMISTIC_LOCKING_ERROR = "optimisticLockingError";


  private static final String ERROR_HANDLES = "errorHandles";

  static {
    IGNORED.add("java.lang.reflect.InvocationTargetException");
    IGNORED.add("java.lang.NullPointerException");
    IGNORED.add("org.apache.olingo.odata2.api.exception.ODataBadRequestException");
    IGNORED.add("org.apache.olingo.odata2.api.ep.EntityProviderException");
    DATABASE = loadJSON();
  }

  private static JsonObject loadJSON() {
    ClassLoader classLoader = QueryManager.class.getClassLoader();
    try (InputStream stream = classLoader.getResourceAsStream("cronapi/database/databases.json")) {
      InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
      JsonElement jsonElement = new JsonParser().parse(reader);
      return jsonElement.getAsJsonObject();
    } catch (Exception e) {
      return new JsonObject();
    }
  }

  private static JsonObject getDataBaseJSON() {
    if (Operations.IS_DEBUG) {
      return loadJSON();
    } else {
      return DATABASE;
    }
  }

  private static String handleDatabaseException(String message, String method, Throwable ex) {

    if (message.contains("cannot be updated because it has changed or been deleted since it was last read")) {
      return Messages.getString(OPTIMISTIC_LOCKING_ERROR);
    }

    for (JsonElement elem : getDataBaseJSON().getAsJsonArray(PRIMARY_KEY_ERROR)) {
      if (message.toLowerCase().contains(elem.getAsString().toLowerCase())) {
        JsonObject obj = RestClient.getRestClient().getQuery();
        if (obj != null && obj.get(ERROR_HANDLES) != null && !obj.get(ERROR_HANDLES).isJsonNull()) {
          obj = obj.get(ERROR_HANDLES).getAsJsonObject();
        }
        if (obj != null && obj.get(PRIMARY_KEY) != null && !obj.get(PRIMARY_KEY).isJsonNull()) {
          return Messages.format(AppMessages.getString(obj.get(PRIMARY_KEY).getAsString().replace("{{", "").replace("}}", "")), AppMessages.getString("error" + method + "Type"));
        } else {
          return Messages.format(Messages.getString(PRIMARY_KEY_ERROR), Messages.getString("error" + method + "Type"));
        }
      }
    }

    for (JsonElement elem : getDataBaseJSON().getAsJsonArray(FOREIGN_KEY_ERROR)) {
      if (message.toLowerCase().contains(elem.getAsString().toLowerCase())) {
        JsonObject obj = RestClient.getRestClient().getQuery();
        if (obj != null && obj.get(ERROR_HANDLES) != null) {
          obj = obj.get(ERROR_HANDLES).getAsJsonObject();
        }
        if (obj != null && obj.get(FOREIGN_KEY) != null && !obj.get(FOREIGN_KEY).isJsonNull()) {
          return Messages.format(obj.get(FOREIGN_KEY).getAsString(), Messages.getString("error" + method + "Type"));
        } else {
          return Messages.format(Messages.getString(FOREIGN_KEY_ERROR), Messages.getString("error" + method + "Type"));
        }
      }
    }

    if (hasThrowable(ex, DatabaseException.class)) {
      Throwable dbException = getThrowable(ex, DatabaseException.class);
      if (!StringUtils.isEmpty(dbException.getCause().getMessage())) {
        message = dbException.getCause().getMessage().trim();
        Matcher matcher = EXCEPTION_NAME_PATTERN.matcher(message);
        while (matcher.find()) {
          message = message.substring(matcher.group(1).length()).trim();
          matcher = EXCEPTION_NAME_PATTERN.matcher(message);
        }
      }
    }

    return message;
  }

  public ErrorResponse(int status, Throwable ex, String method) {
    this.error = getExceptionMessage(ex, method);
    this.status = status;
    this.method = method;

    if (ex != null) {
      StringWriter writer = new StringWriter();
      ex.printStackTrace(new PrintWriter(writer));

      this.stackTrace = writer.toString();
    }
  }

  public String getError() {
    return error;
  }

  public String getMethod() {
    return method;
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

  private static boolean hasIgnoredException(Throwable ex) {
    for (String s : IGNORED) {
      if ((ex.getMessage() != null && ex.getMessage().contains(s)) || ex.getClass().getCanonicalName().equals(s)) {
        return true;
      }
    }

    return false;
  }

  private static boolean hasThrowable(Throwable ex, Class clazz) {
    while (ex != null) {
      if (ex.getClass() == clazz) {
        return true;
      }

      ex = ex.getCause();
    }

    return false;
  }

  private static Throwable getThrowable(Throwable ex, Class clazz) {
    Throwable parent = ex;
    while (parent != null) {
      if (parent.getClass() == clazz) {
        return parent;
      }

      parent = parent.getCause();
    }

    return ex;
  }

  public static RuntimeException createException(Throwable ex, String method) {
    final String message = getExceptionMessage(ex, method);
    return new RuntimeException(message, ex);
  }

  public static String getExceptionMessage(Throwable ex, String method) {
    return getExceptionMessage(ex, method, null);
  }

  public static String getExceptionMessage(Throwable ex, String method, String entity) {

    String message = null;

    if (ex != null) {

      if (entity != null) {
        JsonObject obj = null;
        try {
          obj = QueryManager.getQuery(entity);
        } catch (Exception e) {
          //NoCommande
        }

        if (obj != null && !QueryManager.isNull(obj.get("events")) && !QueryManager.isNull(obj.get("events").getAsJsonObject().get("onError"))) {
          try {
            Map<String, Var> values = new LinkedHashMap<>();
            values.put("exception", Var.valueOf(ex));
            values.put("exceptionMessage", Var.valueOf(ex.getMessage()));
            values.put("data", Var.valueOf(RestClient.getRestClient().getEntity()));
            values.put("primaryKeys", Var.valueOf(RestClient.getRestClient().getKeys()));
            if (RestClient.getRestClient().getKeys() != null && RestClient.getRestClient().getKeys().size() > 0) {
              values.put("primaryKey", Var.valueOf(RestClient.getRestClient().getKeys().get(0)));
            }
            values.put("entityName", Var.valueOf(entity));
            values.put("eventName", Var.valueOf("onError"));
            QueryManager.executeEvent(obj, "onError", values);
          } catch (Exception e) {
            ex = e;
          }
        }
      }

      if (ex.getMessage() != null && !ex.getMessage().trim().isEmpty() && !hasIgnoredException(ex)) {
        message = ex.getMessage();
        Matcher matcher = EXCEPTION_NAME_PATTERN.matcher(message);
        while (matcher.find()) {
          message = message.substring(matcher.group(1).length()).trim();
          matcher = EXCEPTION_NAME_PATTERN.matcher(message);
        }
        if (hasThrowable(ex, javax.persistence.RollbackException.class) ||
            hasThrowable(ex, javax.persistence.PersistenceException.class) ||
            hasThrowable(ex, javax.persistence.OptimisticLockException.class)) {
          message = handleDatabaseException(message, method, ex);
        }
      } else {
        if (ex.getCause() != null) {
          return getExceptionMessage(ex.getCause(), method);
        }
      }
    }

    if (message == null || message.trim().isEmpty()) {
      return Messages.getString("errorNotSpecified");
    }

    return message;

  }
}
