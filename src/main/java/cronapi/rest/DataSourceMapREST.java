package cronapi.rest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cronapi.AppConfig;
import cronapi.QueryManager;
import cronapi.odata.server.ODataConfiguration;
import cronapi.util.Operations;
import org.eclipse.persistence.internal.jpa.deployment.PersistenceUnitProcessor;
import org.eclipse.persistence.internal.jpa.deployment.SEPersistenceUnitInfo;
import org.eclipse.persistence.jpa.Archive;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping(value = "/js/dataSourceMap.js")
public class DataSourceMapREST {

  private static Map<String, DataSourceDetail> mapped;
  private static boolean isDebug = Operations.IS_DEBUG;

  /**
   * Construtor
   **/
  public DataSourceMapREST() {
  }

  public static void cleanCache() {
    mapped = null;
  }

  public void writeMap(Writer out) throws Exception {
    if (mapped == null) {
      synchronized (DataSourceMapREST.class) {
        if (mapped == null) {

          Set<Archive> currentArchives = PersistenceUnitProcessor.findPersistenceArchives();

          String defaultNamespace = null;
          for (Archive archive : currentArchives) {
            List<SEPersistenceUnitInfo> persistenceUnitInfos = PersistenceUnitProcessor.getPersistenceUnits(archive, Thread.currentThread().getContextClassLoader());
            for (SEPersistenceUnitInfo pui : persistenceUnitInfos) {
              defaultNamespace = pui.getPersistenceUnitName();
              break;
            }
            if (defaultNamespace != null) {
              break;
            }
          }

          HashMap<String, DataSourceDetail> mappedAllDs = new HashMap<String, DataSourceDetail>();
          JsonObject customQuery = QueryManager.getJSON();
          for (Map.Entry<String, JsonElement> entry : customQuery.entrySet()) {
            String guid = entry.getKey();
            DataSourceDetail detail = this.getDetail(guid, entry.getValue().getAsJsonObject(), defaultNamespace);
            if (detail.namespace.isEmpty()) {
              mappedAllDs.put(detail.customId, detail);
              mappedAllDs.put(guid, detail);
            } else {
              mappedAllDs.put(detail.namespace + "." + detail.customId, detail);
              mappedAllDs.put(detail.namespace + "." + guid, detail);
            }
          }

          if (AppConfig.exposeLocalEntities()) {
            Set<Archive> archives = PersistenceUnitProcessor.findPersistenceArchives();

            for (Archive archive : archives) {
              List<SEPersistenceUnitInfo> persistenceUnitInfos = PersistenceUnitProcessor.getPersistenceUnits(archive, Thread.currentThread().getContextClassLoader());
              for (SEPersistenceUnitInfo pui : persistenceUnitInfos) {

                String namespace = pui.getPersistenceUnitName();
                for (String clazz : pui.getManagedClassNames()) {
                  String clazzName = clazz.substring(clazz.lastIndexOf(".") + 1);
                  String serviceUrlODATA = String.format(ODataConfiguration.SERVICE_URL + "%s/%s", namespace, clazzName);
                  String serviceUrlApi = String.format("api/cronapi/crud/%s", clazz);

                  DataSourceDetail detail = new DataSourceDetail(namespace, clazz, serviceUrlApi, serviceUrlODATA, true);
                  mappedAllDs.put(clazz.replace(".entity.", "."), detail);
                }
              }
            }
          }


          if (!isDebug) {
            mapped = mappedAllDs;
          } else {
            write(out, mappedAllDs);
          }

        }
      }
    }

    if (mapped != null) {
      write(out, mapped);
    }
  }

  @RequestMapping(method = RequestMethod.GET)
  public void register(HttpServletRequest request, HttpServletResponse response) throws Exception {
    response.setContentType("application/javascript");
    PrintWriter out = response.getWriter();
    writeMap(out);
  }

  private DataSourceDetail getDetail(String guid, JsonObject json, String defaultNamespace) {

    String customId = json.get("customId").getAsString();

    DataSourceDetail detail = null;

    String serviceUrl = json.get("serviceUrl").getAsString();
    serviceUrl = serviceUrl.replace(String.format("/%s/", guid), String.format("/%s/", customId));

    if ("entityFullName".equals(json.get("sourceType").getAsString())) {
      String entityFullName = json.get("entityFullName").getAsString();
      String namespace = entityFullName.substring(0, entityFullName.indexOf(".entity."));
      String serviceUrlODATA = String.format(ODataConfiguration.SERVICE_URL + "%s/%s", namespace, customId);
      detail = new DataSourceDetail(namespace, customId, serviceUrl, serviceUrlODATA, false);
    } else {
      String namespace = defaultNamespace;
      if (!QueryManager.isNull(json.get("baseEntity"))) {
        String entityFullName = json.get("baseEntity").getAsString();
        namespace = entityFullName.substring(0, entityFullName.indexOf(".entity."));
      }
      String serviceUrlODATA = String.format(ODataConfiguration.SERVICE_URL + "%s/%s", namespace, customId);
      detail = new DataSourceDetail(namespace, customId, serviceUrl, serviceUrlODATA, false);
    }
    return detail;
  }

  private void write(Writer out, Map<String, DataSourceDetail> mapped) throws IOException {
    out.write("window.dataSourceMap = window.dataSourceMap || [];\n");

    mapped.forEach((k, v) -> {

      String curr = String.format("window.dataSourceMap[\"%s\"] = { customId: \"%s\", serviceUrl: \"%s\", serviceUrlODATA: \"%s\" };", k, v.customId, v.serviceUrl, v.serviceUrlODATA);

      try {
        out.write(curr + "\n");
      } catch (IOException e) {
        //
      }
    });
  }

  public class DataSourceDetail {

    public DataSourceDetail(String namespace, String customId, String serviceUrl, String serviceUrlODATA, boolean isEntity) {
      this.namespace = namespace;
      this.customId = customId;
      this.serviceUrl = serviceUrl;
      this.serviceUrlODATA = serviceUrlODATA;
      this.isEntity = isEntity;
    }

    public String namespace;
    public String customId;
    public String serviceUrl;
    public String serviceUrlODATA;
    public boolean isEntity;
  }
}
