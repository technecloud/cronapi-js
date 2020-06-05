package cronapi.rest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cronapi.ErrorResponse;
import cronapi.QueryManager;
import cronapi.RestClient;
import cronapi.i18n.Messages;
import cronapi.report.DataSourcesInBand;
import cronapi.report.ReportService;
import cronapp.reports.commons.ReportFront;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@RestController
@RequestMapping("/api/rest")
public class ReportREST {

  private static final Logger log = LoggerFactory.getLogger(ReportREST.class);

  @Autowired
  private ReportService reportService;

  @RequestMapping(value = "/report", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ReportFront> getReport(@RequestBody ReportFront reportFront) {
    if (reportFront == null)
      return ResponseEntity.badRequest().header("Error", "Report is null").body(new ReportFront());
    log.debug("Get report [" + reportFront + "].");
    ReportFront reportResult = reportService.getReport(reportFront.getReportName());
    return ResponseEntity.ok().body(reportResult);
  }

  @RequestMapping(value = "/report/contentasstring", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<String> getContentAsString(@RequestBody ReportFront reportFront, HttpServletResponse response) {
    if (reportFront == null)
      return ResponseEntity.badRequest().header("Error", "Report is null").body("Error read content file");
    String reportName = reportFront.getReportName();
    log.debug("Print report [" + reportName + "]");
    response.setHeader("Content-Disposition", "inline; filename=" + reportName);
    response.setContentType("application/plain");
    String reportResult = reportService.getContentReport(reportName);
    ResponseEntity error = checkSecurity(reportResult);
    if (error != null) {
      return error;
    }
    return ResponseEntity.ok().body(reportResult);
  }

  private ResponseEntity checkSecurity(String reportResult)  {
    JsonObject reportsJson = (JsonObject) new JsonParser().parse(reportResult);
    JsonElement reportsConfig = reportsJson.get("reportConfig");
    if (!QueryManager.isNull(reportsConfig) && !QueryManager.isNull(reportsConfig.getAsJsonObject().get("restSecurity"))) {
      JsonElement getValue = reportsConfig.getAsJsonObject().get("restSecurity").getAsJsonObject().get("get");
      if (!QueryManager.isNull(getValue)) {
        String security = getValue.getAsJsonPrimitive().getAsString();
        if (StringUtils.isNotEmpty(security)) {
          boolean authorized = false;
          String[] roles = security.split(",");
          for (String role : roles) {
            for (GrantedAuthority authority : RestClient.getRestClient().getAuthorities()) {
              if (role.trim().equalsIgnoreCase(authority.getAuthority())) {
                authorized = true;
                break;
              }
            }
          }
          if (!authorized) {
            JsonObject error = new JsonObject();
            error.addProperty("error", Messages.getString("notAllowed"));
            return ResponseEntity.status(403).body(error.toString());
          }
        }
      }
    }

    return null;
  }

  @RequestMapping(value = "/report/getdatasourcesparams", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<DataSourcesInBand> getDataSourcesParams(@RequestBody DataSourcesInBand dataSourcesInBand) {
    if (dataSourcesInBand == null)
      return ResponseEntity.badRequest().header("Error", "Datasources is null").body(new DataSourcesInBand());
    log.debug("Get datasources params");
    DataSourcesInBand dataSourcesParams = reportService.getDataSourcesParams(dataSourcesInBand);
    return ResponseEntity.ok().body(dataSourcesParams);
  }


  @RequestMapping(value = "/report/pdf", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<byte[]> getPDF(@RequestBody ReportFront reportFront, HttpServletResponse response) {
    if (reportFront == null)
      return ResponseEntity.badRequest().header("Error", "Report is null").body(new byte[0]);
    String reportName = reportFront.getReportName();
    log.debug("Print report [" + reportName + "]");
    response.setHeader("Content-Disposition", "inline; filename=" + reportName + ".pdf");
    response.setContentType("application/pdf");
    byte[] reportResult = reportService.getPDF(reportFront);
    return ResponseEntity.ok().body(reportResult);
  }

  @RequestMapping(value = "/report/pdfasfile", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<String> getPDFAsFile(@RequestBody ReportFront reportFront, HttpServletResponse response) {
    if (reportFront == null)
      return ResponseEntity.badRequest().header("Error", "Report is null").body("");
    String reportName = reportFront.getReportName();
    log.debug("Print report [" + reportName + "]");
    response.setHeader("Content-Disposition", "inline; filename=" + reportName + ".pdf");
    response.setContentType("application/plain");
    String reportResult = reportService.getPDFAsFile(reportFront);
    return ResponseEntity.ok().body(reportResult);
  }

  @ExceptionHandler(Throwable.class)
  @ResponseBody
  ResponseEntity<ErrorResponse> handleControllerException(HttpServletRequest req, Throwable ex) {
    ex.printStackTrace();
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex, req.getMethod());
    return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

}
