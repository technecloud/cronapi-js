package cronapi;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cronapi.database.DataSource;
import cronapi.database.DataSourceFilter;
import cronapi.database.DataSourceFilter.DataSourceFilterItem;
import cronapi.database.JPQLConverter;
import cronapi.i18n.Messages;
import cronapi.rest.security.CronappSecurity;
import cronapi.util.Operations;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.ReflectionUtils;

public class QueryManager {

  private static JsonObject JSON;

  private static JsonArray DEFAULT_AUTHORITIES;

  private static File fromFile = null;

  public static boolean DISABLE_AUTH = false;

  static {
    JSON = loadJSON();
    DEFAULT_AUTHORITIES = new JsonArray();
    DEFAULT_AUTHORITIES.add("authenticated");
  }

  private static JsonObject loadJSON() {
    if (fromFile != null) {
      try (InputStream stream = new FileInputStream(fromFile)) {
        InputStreamReader reader = new InputStreamReader(stream);
        JsonElement jsonElement = new JsonParser().parse(reader);
        return jsonElement.getAsJsonObject();
      } catch (Exception e) {
        return new JsonObject();
      }
    } else {
      ClassLoader classLoader = QueryManager.class.getClassLoader();
      try (InputStream stream = classLoader.getResourceAsStream("META-INF/customQuery.json")) {
        InputStreamReader reader = new InputStreamReader(stream);
        JsonElement jsonElement = new JsonParser().parse(reader);
        return jsonElement.getAsJsonObject();
      } catch (Exception e) {
        return new JsonObject();
      }
    }
  }

  public static void loadJSONFromFile(File file) throws IOException {
    fromFile = file;
  }

  public static JsonObject getJSON() {
    if (Operations.IS_DEBUG) {
      return loadJSON();
    } else {
      return JSON;
    }
  }

  public static JsonObject getQuery(String id) {
    JsonObject obj = getJSON().getAsJsonObject(id);
    if (obj == null) {
      for (Map.Entry<String, JsonElement> entry : getJSON().entrySet()) {
        JsonObject customObj = entry.getValue().getAsJsonObject();
        if (!isNull(customObj.get("customId")) && customObj.get("customId").getAsString()
            .equalsIgnoreCase(id)) {
          obj = customObj;
          break;
        }
      }
      if (obj == null) {
        throw new RuntimeException(Messages.getString("queryNotFound"));
      }
    }

    RestClient.getRestClient().setQuery(obj);
    return obj;
  }

  public static String getJPQL(JsonObject query, boolean checkMultitenant) {
    if (!isNull(query.get("query"))) {
      if (query.get("query").isJsonObject()) {
        JsonObject queryObj = query.get("query").getAsJsonObject();
        if (queryObj.get("isRawSql") != null && !queryObj.get("isRawSql").isJsonNull() && queryObj
            .get("isRawSql").getAsBoolean()) {
          return queryObj.get("sqlContent").getAsString();
        } else {
          return JPQLConverter.sqlFromJson(queryObj, checkMultitenant);
        }
      } else {
        return query.get("query").getAsString();
      }
    }
    return null;
  }

  public static String getType(JsonObject obj) {
    if (obj.get("sourceType") != null && !obj.get("sourceType").isJsonNull()) {
      return obj.get("sourceType").getAsString();
    }

    return "entityFullName";
  }

  public static void checkSecurity(JsonObject obj, String verb) {
    checkSecurity(obj, verb, true);
  }

  public static void checkSecurity(JsonObject obj, String verb, boolean checkAuthorities) {
    if (DISABLE_AUTH) {
      return;
    }

    if (!obj.getAsJsonObject("verbs").get(verb).getAsBoolean()) {
      throw new RuntimeException(Messages.format(Messages.getString("verbNotAllowed"), verb));
    }

    if (checkAuthorities) {
      boolean authorized = false;

      JsonElement auth = obj.getAsJsonObject("verbs").get(verb + "Authorities");
      JsonArray authorities = null;
      if (!isNull(auth) && auth.getAsJsonArray().size() > 0) {
        authorities = auth.getAsJsonArray();
      } else {
        authorities = DEFAULT_AUTHORITIES;
      }

      for (JsonElement a : authorities) {
        String role = a.getAsString();
        if (role.equalsIgnoreCase("authenticated")) {
          authorized = RestClient.getRestClient().getUser() != null;
          if (authorized) {
            break;
          }
        }
        if (role.equalsIgnoreCase("permitAll") || role.equalsIgnoreCase("public")) {
          authorized = true;
          break;
        }
        for (GrantedAuthority authority : RestClient.getRestClient().getAuthorities()) {
          if (role.equalsIgnoreCase(authority.getAuthority())) {
            authorized = true;
            break;
          }
        }

        if (authorized) {
          break;
        }
      }

      if (!authorized) {
        throw new RuntimeException(Messages.getString("notAllowed"));
      }
    }
  }

  public static boolean isNull(JsonElement value) {
    return value == null || value.isJsonNull();
  }

  public static void addDefaultValues(JsonObject query, Var ds, boolean onlyNull) {
    if (!isNull(query.get("defaultValues"))) {
      for (Map.Entry<String, JsonElement> entry : query.get("defaultValues").getAsJsonObject()
          .entrySet()) {
        if (!entry.getValue().isJsonNull()) {
          Var value;
          if (entry.getValue().isJsonObject()) {
            JsonObject event = entry.getValue().getAsJsonObject();
            Var name = Var
                .valueOf(event.get("blocklyClass").getAsString() + ":" + event.get("blocklyMethod")
                    .getAsString());
            try {
              value = Operations.callBlockly(name, Var.valueOf(ds));
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          } else {
            value = parseExpressionValue(entry.getValue());
          }

          if (onlyNull) {
            if (ds.getField(entry.getKey()) == null) {
              ds.setField(entry.getKey(), value);
            }
          } else {
            ds.setField(entry.getKey(), value);
          }
        }
      }
    }
  }

  public static Var executeEvent(JsonObject query, Object ds, String eventName) {
    return executeEvent(query, ds, eventName, null, null);
  }

  public static Var executeEvent(JsonObject query, Object ds, String eventName, List<Object> keys, String entityName) {
    JsonObject events = query.getAsJsonObject("events");
    if (!isNull(events)) {
      if (!isNull(events.get(eventName))) {
        JsonObject event = events.getAsJsonObject(eventName);
        Var name = Var
            .valueOf(event.get("blocklyClass").getAsString() + ":" + event.get("blocklyMethod")
                .getAsString());
        try {
          if (query.getAsJsonArray("queryParamsValues") != null && query.getAsJsonArray("queryParamsValues").size() > 0) {
            return callBlocly(event, name, ds, keys, entityName, eventName);
          } else {
            return Operations.callBlockly(name, Var.valueOf(ds));
          }
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }

    return null;
  }

  private static Var callBlocly(JsonObject event, Var methodName, Object ds, List<Object> keys, String entityName, String eventName) {
    try {
      if (event.get("blocklyParams") != null) {
        JsonArray bloclyParams = event.get("blocklyParams").getAsJsonArray();
        Var params[] = new Var[bloclyParams.size()];
        for (int i = 0; i < bloclyParams.size(); i++) {
          JsonObject param = bloclyParams.get(i).getAsJsonObject();
          Map<String, Var> customValues = new LinkedHashMap<>();
          customValues.put("data", Var.valueOf(ds));
          if (keys != null && keys.size() > 0) {
            customValues.put("primaryKeys", Var.valueOf(keys));
            customValues.put("primaryKey", Var.valueOf(keys.get(0)));
          }
          customValues.put("entityName", Var.valueOf(entityName));
          customValues.put("eventName", Var.valueOf(eventName));

          params[i] = parseExpressionValue(ds, param.get("value"), customValues);

        }

        return Operations.callBlockly(methodName, params);
      } else {
        return Operations.callBlockly(methodName, Var.VAR_NULL);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static void executeEvent(JsonObject query, String eventName, Map<String, Var> customValues) {
    JsonObject events = query.getAsJsonObject("events");
    if (!isNull(events)) {
      if (!isNull(events.get(eventName))) {
        JsonObject event = events.getAsJsonObject(eventName);
        Var name = Var
            .valueOf(event.get("blocklyClass").getAsString() + ":" + event.get("blocklyMethod")
                .getAsString());

        Var params[] = new Var[0];
        if (event.get("blocklyParams") != null) {
          JsonArray bloclyParams = event.get("blocklyParams").getAsJsonArray();
          params = new Var[bloclyParams.size()];
          for (int i = 0; i < bloclyParams.size(); i++) {
            JsonObject param = bloclyParams.get(i).getAsJsonObject();
            customValues.put("eventName", Var.valueOf(eventName));
            params[i] = parseExpressionValue(null, param.get("value"), customValues);
          }
        }

        try {
          Operations.callBlockly(name, params);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  public static void executeEvent(JsonObject query, String eventName, Var... params) {
    JsonObject events = query.getAsJsonObject("events");
    if (!isNull(events)) {
      if (!isNull(events.get(eventName))) {
        JsonObject event = events.getAsJsonObject(eventName);
        Var name = Var
            .valueOf(event.get("blocklyClass").getAsString() + ":" + event.get("blocklyMethod")
                .getAsString());
        try {
          Operations.callBlockly(name, params);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  public static void executeNavigateEvent(JsonObject query, DataSource ds) {
    JsonObject events = query.getAsJsonObject("events");
    if (!isNull(events)) {
      if (!isNull(events.get("onNavigate"))) {
        JsonObject event = events.getAsJsonObject("onNavigate");

        Var name = Var
            .valueOf(event.get("blocklyClass").getAsString() + ":" + event.get("blocklyMethod")
                .getAsString());
        Var dsVar = Var.valueOf(ds);

        int current = ds.getCurrent();
        int size = ds.getPage().getContent().size();
        for (int i = 0; i < size; i++) {
          try {
            Operations.callBlockly(name, dsVar);
            ds.nextOnPage();
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }

        ds.setCurrent(current);
      }
    }
  }

  public static Var doExecuteBlockly(JsonObject blockly, String method, Var... params)
      throws Exception {
    String function = blockly.get("blocklyMethod").getAsString();

    if (method != null && !isNull(blockly.get("blockly" + method.toUpperCase() + "Method"))) {
      function = blockly.get("blockly" + method.toUpperCase() + "Method").getAsString();
    }

    Var name = Var.valueOf(blockly.get("blocklyClass").getAsString() + ":" + function);
    return Operations.callBlockly(name, params);
  }

  public static Var executeBlockly(JsonObject query, String method, Var... vars) {
    if (!isNull(query.getAsJsonObject("blockly"))) {
      try {
        return doExecuteBlockly(query.getAsJsonObject("blockly"), method, vars);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    return Var.VAR_NULL;
  }

  public static Var getParameterValue(JsonObject customQuery, String param, Map<String, Var> customValues) {
    JsonArray paramValues = customQuery.getAsJsonArray("queryParamsValues");

    if (paramValues != null) {
      for (int x = 0; x < paramValues.size(); x++) {
        JsonElement prv = paramValues.get(x);
        if (param.equals(prv.getAsJsonObject().get("fieldName").getAsString())) {

          if (isNull(prv.getAsJsonObject().get("fieldValue"))) {
            return Var.VAR_NULL;
          }

          return parseExpressionValue(null, ((JsonObject) prv).get("fieldValue"), customValues);
        }
      }
    }

    return Var.VAR_NULL;
  }

  public static String getBlocklyMethod(JsonObject query, String method) {
    JsonObject blockly = query.getAsJsonObject("blockly");
    String function = blockly.get("blocklyMethod").getAsString();

    if (method != null && !isNull(blockly.get("blockly" + method.toUpperCase() + "Method"))) {
      function = blockly.get("blockly" + method.toUpperCase() + "Method").getAsString();
    }

    return function;
  }

  private static void addIgnoreField(String field) {
    HashSet<String> ignores = (HashSet<String>) RestClient.getRestClient().getRequest()
        .getAttribute("BeanPropertyFilter");
    if (ignores == null) {
      ignores = new HashSet<>();
      RestClient.getRestClient().getRequest().setAttribute("BeanPropertyFilter", ignores);
    }

    ignores.add(field);
  }

  public static boolean isFieldAuthorized(Class clazzToCheck, String key, String method)
      throws Exception {
    RestClient client = RestClient.getRestClient();
    if (client.getRequest() != null) {
      if (clazzToCheck != null) {

        Class clazz = clazzToCheck;
        if (clazz != null) {
          Field field = ReflectionUtils.findField(clazz, key);
          if (field != null) {
            Annotation security = field.getAnnotation(CronappSecurity.class);

            if (security instanceof CronappSecurity) {
              CronappSecurity cronappSecurity = (CronappSecurity) security;
              try {
                Method methodPermission = cronappSecurity.getClass().getMethod(method == null ? client.getMethod().toLowerCase() : method.toLowerCase());

                if (methodPermission != null) {
                  String value = (String) methodPermission.invoke(cronappSecurity);

                  if (value != null && !value.isEmpty()) {
                    boolean authorized = false;

                    String[] authorities = value.trim().split(";");

                    for (String role : authorities) {
                      if (role.equalsIgnoreCase("authenticated")) {
                        authorized = RestClient.getRestClient().getUser() != null;
                        if (authorized) {
                          break;
                        }
                      }
                      if (role.equalsIgnoreCase("permitAll") || role.equalsIgnoreCase("public")) {
                        authorized = true;
                        break;
                      }
                      for (GrantedAuthority authority : RestClient.getRestClient().getAuthorities()) {
                        if (role.equalsIgnoreCase(authority.getAuthority())) {
                          authorized = true;
                          break;
                        }
                      }

                      if (authorized) {
                        break;
                      }
                    }

                    if (!authorized) {
                      return false;
                    }
                  }
                }

              } catch (Exception e) {
                //
              }
            }
          }
        }

      }
    }
    return true;
  }

  public static void checkSecurity(Class clazz, String method) throws Exception {
    if (DISABLE_AUTH) {
      return;
    }
    if (!AppConfig.exposeLocalEntities()) {
      throw new RuntimeException(Messages.getString("notAllowed"));
    }

    Annotation security = clazz.getAnnotation(CronappSecurity.class);
    boolean authorized = false;

    if (security instanceof CronappSecurity) {
      CronappSecurity cronappSecurity = (CronappSecurity) security;
      Method methodPermission = cronappSecurity.getClass().getMethod(method.toLowerCase());
      if (methodPermission != null) {
        String value = (String) methodPermission.invoke(cronappSecurity);
        if (value == null || value.trim().isEmpty()) {
          value = "authenticated";
        }

        String[] authorities = value.trim().split(";");

        for (String role : authorities) {
          if (role.equalsIgnoreCase("authenticated")) {
            authorized = RestClient.getRestClient().getUser() != null;
            if (authorized) {
              break;
            }
          }
          if (role.equalsIgnoreCase("permitAll") || role.equalsIgnoreCase("public")) {
            authorized = true;
            break;
          }
          for (GrantedAuthority authority : RestClient.getRestClient().getAuthorities()) {
            if (role.equalsIgnoreCase(authority.getAuthority())) {
              authorized = true;
              break;
            }
          }

          if (authorized) {
            break;
          }
        }
      }
    }

    if (!authorized) {
      throw new RuntimeException(Messages.getString("notAllowed"));
    }
  }

  public static boolean isFieldAuthorized(JsonObject query, String field, String method)
      throws Exception {
    if (!isNull(query.get("security"))) {
      JsonObject security = query.get("security").getAsJsonObject();

      JsonElement permissionElement = security.get(field);

      if (!isNull(permissionElement)) {
        JsonObject permission = permissionElement.getAsJsonObject();
        if (!isNull(permission.get(method.toLowerCase()))) {
          String[] roles = permission.get(method.toLowerCase()).getAsString().toLowerCase()
              .split(";");

          boolean authorized = false;

          if (ArrayUtils.contains(roles, "public") || ArrayUtils.contains(roles, "permitAll")) {
            authorized = true;
          }

          if (ArrayUtils.contains(roles, "authenticated")) {
            authorized = RestClient.getRestClient().getUser() != null;
          }

          if (!authorized) {
            for (GrantedAuthority authority : RestClient.getRestClient().getAuthorities()) {
              if (ArrayUtils.contains(roles, authority.getAuthority().toLowerCase())) {
                authorized = true;
                break;
              }
            }
          }

          return authorized;
        }
      }
    }

    return true;
  }

  public static void checkFieldSecurity(JsonObject query, Object ds, String method)
      throws Exception {
    if (DISABLE_AUTH) {
      return;
    }

    if (!isNull(query.get("security"))) {
      JsonObject security = query.get("security").getAsJsonObject();

      for (Entry<String, JsonElement> entry : security.entrySet()) {
        if (!isNull(entry.getValue())) {
          JsonObject permission = entry.getValue().getAsJsonObject();
          if (!isNull(permission.get(method.toLowerCase()))) {
            String[] roles = permission.get(method.toLowerCase()).getAsString().toLowerCase()
                .split(";");

            boolean authorized = false;

            if (ArrayUtils.contains(roles, "public") || ArrayUtils.contains(roles, "permitAll")) {
              authorized = true;
            }

            if (ArrayUtils.contains(roles, "authenticated")) {
              authorized = RestClient.getRestClient().getUser() != null;
              ;
            }

            if (!authorized) {
              for (GrantedAuthority authority : RestClient.getRestClient().getAuthorities()) {
                if (ArrayUtils.contains(roles, authority.getAuthority().toLowerCase())) {
                  authorized = true;
                  break;
                }
              }
            }

            if (!authorized) {
              if (ds instanceof UriInfo) {
                UriInfo uriView = (UriInfo) ds;
                EdmEntityType entityType = uriView.getTargetEntitySet().getEntityType();
                if (uriView.getSelect().isEmpty()) {
                  for (String propertyName : entityType.getPropertyNames()) {
                    EdmProperty property = (EdmProperty) entityType.getProperty(propertyName);
                  }
                }
              }
            }

            if (!authorized && method.equalsIgnoreCase("GET")) {
              addIgnoreField(((DataSource) ds).getDomainClass().getName() + "#" + entry.getKey());
            }

            if (!authorized && !method.equalsIgnoreCase("GET")) {
              if (ds instanceof Var) {
                ((Var) ds).getObjectAsMap().remove(entry.getKey());
              } else if (ds instanceof RestBody) {
                ((RestBody) ds).getInputs()[0].getObjectAsMap().remove(entry.getKey());
              }
            }
          }
        }
      }
    }
  }

  public static void checkFilterSecurity(JsonObject query, DataSourceFilter filter) {
    if (DISABLE_AUTH) {
      return;
    }

    if (!isNull(query.get("security")) && filter != null && filter.getItems().size() > 0) {
      JsonObject security = query.get("security").getAsJsonObject();

      for (DataSourceFilterItem item : filter.getItems()) {
        if (!isNull(security.get(item.key))) {
          JsonObject permission = security.get(item.key).getAsJsonObject();
          if (!isNull(permission.get("filter"))) {
            String[] roles = permission.get("filter").getAsString().toLowerCase().split(";");
            boolean authorized = false;
            for (String role : roles) {
              if (role.equalsIgnoreCase("public") || role.equalsIgnoreCase("permitAll")) {
                authorized = true;
                break;
              }

              if (role.equalsIgnoreCase("authenticated")) {
                authorized = RestClient.getRestClient().getUser() != null;
                if (authorized) {
                  break;
                }
              }

              if (!authorized) {
                for (GrantedAuthority authority : RestClient.getRestClient().getAuthorities()) {
                  if (authority.getAuthority().equalsIgnoreCase(role)) {
                    authorized = true;
                    break;
                  }
                }
              }

              if (authorized) {
                break;
              }
            }

            if (!authorized) {
              throw new RuntimeException(Messages.getString("notAllowed"));
            }
          }
        }
      }
    }
  }

  public static void checkEntityFilterSecurity(Object obj, List<String> filters) {
    if (DISABLE_AUTH) {
      return;
    }

    Class clazz = obj instanceof Class ? (Class) obj : obj.getClass();

    for (String filter : filters) {
      Field f = null;
      try {
        f = clazz.getDeclaredField(filter);
      } catch (Exception e) {
        //NoCommand
      }

      if (f != null) {
        Annotation annotation = f.getAnnotation(CronappSecurity.class);
        boolean authorized = true;
        if (annotation != null) {
          CronappSecurity security = (CronappSecurity) annotation;
          String authoritiesStr = security.filter();
          String[] authorities;
          if (authoritiesStr != null && !authoritiesStr.trim().isEmpty()) {
            authorized = false;
            authorities = authoritiesStr.trim().split(";");
            for (String role : authorities) {
              if (role.equalsIgnoreCase("authenticated")) {
                authorized = RestClient.getRestClient().getUser() != null;
                if (authorized) {
                  break;
                }
              }
              if (role.equalsIgnoreCase("permitAll") || role.equalsIgnoreCase("public")) {
                authorized = true;
                break;
              }
              for (GrantedAuthority authority : RestClient.getRestClient().getAuthorities()) {
                if (role.equalsIgnoreCase(authority.getAuthority())) {
                  authorized = true;
                  break;
                }
              }

            }
          }
        }
        if (!authorized) {
          throw new RuntimeException(Messages.getString("notAllowed"));
        }
      }
    }
  }

  public static void checkFilterSecurity(JsonObject query, List<String> filter) {
    if (DISABLE_AUTH) {
      return;
    }

    if (!isNull(query.get("security")) && filter != null && filter.size() > 0) {
      JsonObject security = query.get("security").getAsJsonObject();

      for (String item : filter) {
        if (!isNull(security.get(item))) {
          JsonObject permission = security.get(item).getAsJsonObject();
          if (!isNull(permission.get("filter"))) {
            String[] roles = permission.get("filter").getAsString().toLowerCase().split(";");
            boolean authorized = false;
            for (String role : roles) {
              if (role.equalsIgnoreCase("public") || role.equalsIgnoreCase("permitAll")) {
                authorized = true;
                break;
              }

              if (role.equalsIgnoreCase("authenticated")) {
                authorized = RestClient.getRestClient().getUser() != null;
                if (authorized) {
                  break;
                }
              }

              if (!authorized) {
                for (GrantedAuthority authority : RestClient.getRestClient().getAuthorities()) {
                  if (authority.getAuthority().equalsIgnoreCase(role)) {
                    authorized = true;
                    break;
                  }
                }
              }

              if (authorized) {
                break;
              }
            }

            if (!authorized) {
              throw new RuntimeException(Messages.getString("notAllowed"));
            }
          }
        }
      }
    }
  }

  public static Map<String, Object> getCalcFieldValues(JsonObject query, Object bean) {
    return getFieldValues("calcFields", query, bean);
  }

  public static Map<String, Object> getDefaultValues(JsonObject query, Object bean) {
    return getFieldValues("defaultValues", query, bean);
  }

  private static Map<String, Object> getFieldValues(String prefix, JsonObject query, Object bean) {

    JsonObject calcObj = null;
    Map<String, Object> result = null;

    if (!isNull(query.get(prefix))) {
      calcObj = query.get(prefix).getAsJsonObject();
    }

    if (calcObj != null) {

      JsonObject securityObj = null;

      if (!isNull(query.get(prefix + "Security"))) {
        securityObj = query.get(prefix + "Security").getAsJsonObject();
      }

      for (Entry<String, JsonElement> entry : calcObj.entrySet()) {
        String name = entry.getKey();
        boolean authorized = true;

        if (securityObj != null) {
          if (!isNull(securityObj.get(name)) && (!isNull(
              securityObj.get(name).getAsJsonObject().get("get")))) {
            String security = securityObj.get(name).getAsJsonObject().get("get").getAsString();
            if (security == null) {
              security = "public";
            }
            String[] roles = security.split(";");

            authorized = false;
            for (String role : roles) {
              if (role.equalsIgnoreCase("public") || role.equalsIgnoreCase("permitAll")) {
                authorized = true;
                break;
              }

              if (role.equalsIgnoreCase("authenticated")) {
                authorized = RestClient.getRestClient().getUser() != null;
                if (authorized) {
                  break;
                }
              }

              if (!authorized) {
                for (GrantedAuthority authority : RestClient.getRestClient().getAuthorities()) {
                  if (authority.getAuthority().equalsIgnoreCase(role)) {
                    authorized = true;
                    break;
                  }
                }
              }

              if (authorized) {
                break;
              }
            }
          }
        }

        if (authorized) {
          JsonElement element = calcObj.get(name);
          if (!isNull(element)) {
            Var value;

            if (element.isJsonPrimitive()) {
              value = parseExpressionValue(element);
            } else {
              try {
                JsonObject object = element.getAsJsonObject();
                String method = object.get("blocklyMethod").getAsString();
                Var call = Var.valueOf(object.get("blocklyClass").getAsString() + ":" + method);
                value = callBlocly(object, call, bean, null, null, null);
              } catch (Exception e) {
                value = Var.valueOf("ERROR: " + e.getMessage());
              }
            }

            if (result == null) {
              result = new HashMap<>();
            }

            result.put(name, value.getObject());
          }
        }
      }
    }

    return result;
  }

  public static Var parseExpressionValue(Object ds, JsonElement element, Map<String, Var> customValues) {
    Var value = Var.VAR_NULL;
    if (!isNull(element)) {
      if (element.isJsonPrimitive()) {
        if (element.getAsJsonPrimitive().isBoolean()) {
          value = Var.valueOf(element.getAsJsonPrimitive().getAsBoolean());
        } else if (element.getAsJsonPrimitive().isNumber()) {
          value = Var.valueOf(element.getAsJsonPrimitive().getAsNumber());
        } else {
          if (!element.getAsJsonPrimitive().getAsString().isEmpty()) {
            if (customValues != null && ds != null && element.getAsJsonPrimitive().getAsString().startsWith("data.")) {
              String key = element.getAsJsonPrimitive().getAsString().substring(5);
              return Var.valueOf(ds).getField(key);
            }
            if (customValues != null && customValues.containsKey(element.getAsJsonPrimitive().getAsString())) {
              value = customValues.get(element.getAsJsonPrimitive().getAsString());
            } else {
              value = Var.eval(element.getAsJsonPrimitive().getAsString());
            }
          }
        }
      } else if (element.isJsonObject()) {
        JsonObject blockly = element.getAsJsonObject();

        if ("java".equals(blockly.get("blocklyLanguage").getAsString())) {

          JsonObject jsonCallBlockly = new JsonObject();
          jsonCallBlockly.add("blockly", blockly);
          String method = blockly.get("blocklyMethod").getAsString();

          Var[] blocklyParams = new Var[0];

          if (!isNull(blockly.get("blocklyParams"))) {
            JsonArray params = blockly.getAsJsonArray("blocklyParams");
            blocklyParams = new Var[params.size()];
            for (int countBlocklys = 0; countBlocklys < params.size(); countBlocklys++) {
              JsonObject valueObj = params.get(countBlocklys).getAsJsonObject();
              blocklyParams[countBlocklys] = parseExpressionValue(valueObj.get("value"));
            }
          }

          value = QueryManager.executeBlockly(jsonCallBlockly, method, blocklyParams);
        }
      }
    }

    return value;
  }

  public static Var parseExpressionValue(JsonElement element) {
    return parseExpressionValue(null, element, null);
  }

  public static void addCalcFields(JsonObject query, DataSource ds) {
    addCalcFields(query, ds, true);
  }

  public static void addCalcFields(JsonObject query, Object ds, boolean post) {
    if (!isNull(query.get("calcFields")) && RestClient.getRestClient() != null
        && RestClient.getRestClient().getRequest() != null) {
      for (Entry<String, JsonElement> entry : query.get("calcFields").getAsJsonObject()
          .entrySet()) {
        LinkedHashMap<String, JsonElement> newProperties = (LinkedHashMap<String, JsonElement>) RestClient
            .getRestClient().getRequest()
            .getAttribute("NewBeanProperty");
        if (newProperties == null) {
          newProperties = new LinkedHashMap<>();
          RestClient.getRestClient().getRequest().setAttribute("NewBeanProperty", newProperties);
        }

        boolean authorized = true;

        if (!isNull(query.get("calcFieldsSecurity"))) {
          JsonObject obj = query.get("calcFieldsSecurity").getAsJsonObject();
          if (!isNull(obj.get(entry.getKey())) && (!isNull(
              obj.get(entry.getKey()).getAsJsonObject().get("get")))) {
            String security = obj.get(entry.getKey()).getAsJsonObject().get("get").getAsString();
            if (security == null) {
              security = "public";
            }
            String[] roles = security.split(";");

            authorized = false;
            for (String role : roles) {
              if (role.equalsIgnoreCase("public") || role.equalsIgnoreCase("permitAll")) {
                authorized = true;
                break;
              }

              if (role.equalsIgnoreCase("authenticated")) {
                authorized = RestClient.getRestClient().getUser() != null;
                if (authorized) {
                  break;
                }
              }

              if (!authorized) {
                for (GrantedAuthority authority : RestClient.getRestClient().getAuthorities()) {
                  if (authority.getAuthority().equalsIgnoreCase(role)) {
                    authorized = true;
                    break;
                  }
                }
              }

              if (authorized) {
                break;
              }
            }
          }
        }

        if (authorized) {
          if (post) {
            if (ds instanceof DataSource) {
              newProperties
                  .put(((DataSource) ds).getEntity() + "." + entry.getKey(), entry.getValue());
            }
          } else {
            try {
              cronapi.json.Operations
                  .setJsonOrMapField(Var.valueOf(ds), Var.valueOf(entry.getKey()),
                      Var.valueOf(entry.getValue()));
            } catch (Exception e) {
              // Abafa
            }
          }
        }
      }
    }
  }

  public static void checkMultiTenant(JsonObject query, DataSource ds) {
    if (!isNull(query.get("multiTenant")) && !query.get("multiTenant").getAsBoolean()) {
      ds.disableMultiTenant();
    }

    if (!isNull(query.get("query"))) {
      if (query.get("query").isJsonObject()) {
        JsonObject obj = query.get("query").getAsJsonObject();

        if (!isNull(obj.get("multiTenant")) && !obj.get("multiTenant").getAsBoolean()) {
          ds.disableMultiTenant();
        }
      } else {
        String jpql = query.get("query").getAsString();
        boolean containsNoTenant = jpql.contains("/*notenant*/");
        if (containsNoTenant) {
          ds.disableMultiTenant();
          jpql = jpql.replace("/*notenant*/", "");
          query.addProperty("query", jpql);
        }
      }
    }
  }
}
