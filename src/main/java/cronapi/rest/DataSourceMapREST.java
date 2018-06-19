package cronapi.rest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cronapi.QueryManager;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/js/dataSourceMap.js")
public class DataSourceMapREST {

  private static Map<String, DataSourceDetail> mapped;

  /**
   * Construtor
   **/
  public DataSourceMapREST (){
  }


  @RequestMapping(method = RequestMethod.GET)
  public void register(HttpServletRequest request, HttpServletResponse response) throws Exception {
    response.setContentType("application/javascript");
    PrintWriter out = response.getWriter();

    if(mapped == null) {
      synchronized(DataSourceMapREST.class) {
        if(mapped == null) {
          mapped = new HashMap<String, DataSourceDetail>();

          JsonObject customQuery = QueryManager.getJSON();

          for (Map.Entry<String, JsonElement> entry : customQuery.entrySet()) {
            String guid = entry.getKey();
            DataSourceDetail detail = this.getDetail(guid, entry.getValue().getAsJsonObject());
            mapped.put(guid, detail);
          }

        }
        write(out, mapped);
      }
    }
    else if (mapped != null) {
      write(out, mapped);
    }

  }

  private DataSourceDetail getDetail(String guid, JsonObject json) {

    String customId = json.get("customId").getAsString();

    DataSourceDetail detail = null;

    String serviceUrl = json.get("serviceUrl").getAsString();
    serviceUrl = serviceUrl.replace(String.format("/%s/", guid), String.format("/%s/", customId));

    if ("entityFullName".equals(json.get("sourceType").getAsString())) {
      String entityFullName = json.get("entityFullName").getAsString();
      String[] splited = entityFullName.split(Pattern.quote("."));
      String rootPackage = splited[0];
      String serviceUrlODATA = String.format("api/cronapi/odata/v2/%s", rootPackage);
      serviceUrlODATA = serviceUrl.replace("api/cronapi/query", serviceUrlODATA);
      detail = new DataSourceDetail(customId, serviceUrl, serviceUrlODATA);
    }
    else {
      detail = new DataSourceDetail(customId, serviceUrl, "");
    }
    return detail;
  }

  private void write(PrintWriter out, Map<String, DataSourceDetail> mapped) {
    out.println("window.dataSourceMap = window.dataSourceMap || [];");

    mapped.forEach((k,v) -> {

      String curr = String.format("window.dataSourceMap[\"%s\"] = { customId: \"%s\", serviceUrl: \"%s\", serviceUrlODATA: \"%s\" };",
          k,
          v.customId,
          v.serviceUrl,
          v.serviceUrlODATA
      );

      out.println(curr);

    });
  }

  public class DataSourceDetail {

    public DataSourceDetail(String customId, String serviceUrl, String serviceUrlODATA) {
      this.customId = customId;
      this.serviceUrl = serviceUrl;
      this.serviceUrlODATA = serviceUrlODATA;
    }

    public String customId;
    public String serviceUrl;
    public String serviceUrlODATA;
  }
}
