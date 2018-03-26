package cronapi;

import cronapi.database.DataSourceFilter.DataSourceFilterItem;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.*;
import cronapi.database.DataSourceFilter;
import cronapi.database.JPQLConverter;
import java.util.Map.Entry;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.security.core.GrantedAuthority;

import cronapi.database.DataSource;
import cronapi.i18n.Messages;
import cronapi.util.Operations;

import javax.json.Json;

public class QueryManager {

  private static JsonObject JSON;

  private static JsonArray DEFAULT_AUTHORITIES;

  static {
    JSON = loadJSON();
    DEFAULT_AUTHORITIES = new JsonArray();
    DEFAULT_AUTHORITIES.add("authenticated");
  }

  private static JsonObject loadJSON() {
    ClassLoader classLoader = QueryManager.class.getClassLoader();
    try (InputStream stream = classLoader.getResourceAsStream("META-INF/customQuery.json")) {
      InputStreamReader reader = new InputStreamReader(stream);
      JsonElement jsonElement = new JsonParser().parse(reader);
      return jsonElement.getAsJsonObject();
    } catch (Exception e) {
      return new JsonObject();
    }
  }

  private static JsonObject getJSON() {
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

  public static String getJPQL(JsonObject query) {
    if (!isNull(query.get("query"))) {
      if (query.get("query").isJsonObject()) {
        JsonObject queryObj = query.get("query").getAsJsonObject();
        if (queryObj.get("isRawSql") != null && !queryObj.get("isRawSql").isJsonNull() && queryObj
            .get("isRawSql").getAsBoolean()) {
          return queryObj.get("sqlContent").getAsString();
        } else {
          return JPQLConverter.sqlFromJson(queryObj);
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

  private static boolean isNull(JsonElement value) {
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
            value = Var.valueOf(entry.getValue().getAsString());
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

  public static void executeEvent(JsonObject query, Object ds, String eventName) {
    JsonObject events = query.getAsJsonObject("events");
    if (!isNull(events)) {
      if (!isNull(events.get(eventName))) {
        JsonObject event = events.getAsJsonObject(eventName);
        Var name = Var
            .valueOf(event.get("blocklyClass").getAsString() + ":" + event.get("blocklyMethod")
                .getAsString());
        try {
          Operations.callBlockly(name, Var.valueOf(ds));
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

    if (!isNull(blockly.get("blockly" + method + "Method"))) {
      function = blockly.get("blockly" + method + "Method").getAsString();
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

  private static void addIgnoreField(String field) {
    HashSet<String> ignores = (HashSet<String>) RestClient.getRestClient().getRequest()
        .getAttribute("BeanPropertyFilter");
    if (ignores == null) {
      ignores = new HashSet<>();
      RestClient.getRestClient().getRequest().setAttribute("BeanPropertyFilter", ignores);
    }

    ignores.add(field);
  }

  public static void checkFieldSecurity(JsonObject query, Object ds, String method)
      throws Exception {
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

            if (!authorized && method.equalsIgnoreCase("GET")) {
              addIgnoreField(((DataSource) ds).getDomainClass().getName() + "#" + entry.getKey());
            }

            if (!authorized && !method.equalsIgnoreCase("GET")) {
              if (ds instanceof Var) {
                ((Var) ds).getObjectAsMap().remove(entry.getKey());
              } else {
                ((RestBody) ds).getInputs()[0].getObjectAsMap().remove(entry.getKey());
              }
            }
          }
        }
      }
    }
  }

    public static void checkFilterSecurity (JsonObject query, DataSourceFilter filter){
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

    public static void addCalcFields (JsonObject query, DataSource ds){
      addCalcFields(query, ds, true);
    }

    public static void addCalcFields (JsonObject query, Object ds,boolean post){
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

    public static void checkMultiTenant(JsonObject query, DataSource ds){
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
