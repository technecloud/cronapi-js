package cronapi.report;

import com.google.gson.*;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.stimulsoft.base.exception.StiException;
import com.stimulsoft.base.serializing.StiDeserializationException;
import com.stimulsoft.report.StiExportManager;
import com.stimulsoft.report.StiOptions.Services;
import com.stimulsoft.report.StiReport;
import com.stimulsoft.report.StiSerializeManager;
import com.stimulsoft.report.enums.StiExportFormat;
import com.stimulsoft.report.export.service.StiExportService;
import com.stimulsoft.report.export.settings.StiHtml5ExportSettings;
import com.stimulsoft.report.export.settings.StiHtmlExportSettings;
import com.stimulsoft.report.export.settings.StiPdfExportSettings;
import com.stimulsoft.report.export.tools.html.StiHtmlExportQuality;
import cronapi.*;
import cronapi.report.DataSourcesInBand.FieldParam;
import cronapi.report.DataSourcesInBand.ParamValue;
import cronapi.report.odata.StiODataDatabase;
import cronapi.report.odata.StiODataSource;
import cronapi.rest.DownloadREST;
import cronapp.reports.PrintDesign;
import cronapp.reports.ReportExport;
import cronapp.reports.ReportManager;
import cronapp.reports.commons.Functions;
import cronapp.reports.commons.Parameter;
import cronapp.reports.commons.ParameterType;
import cronapp.reports.commons.ReportFront;
import cronapp.reports.j4c.dataset.J4CDataset;
import cronapp.reports.j4c.dataset.J4CEntity;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExpression;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.olingo.odata2.client.api.ODataClient;
import org.apache.olingo.odata2.client.api.uri.URIBuilder;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.sql.DataSource;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ReportService {
  static {
    com.stimulsoft.base.licenses.StiLicense.setKey("" +
        "6vJhGtLLLz2GNviWmUTrhSqnOItdDwjBylQzQcAOiHl4mF8Yy+Msl8Mjp+nbkDv52zYAIT+dpsXLWIrkoWUKLuRM23" +
        "NSg8pIvYh6tBo4G/ZbeRpxW0S6pW7OFk7po8BkktyA+vHtfRKYFAO4H+qoK6JBlRbQjOtO9vDgdcIfkLIfhwrbQhvh" +
        "yaJMnkga7SqJ2c181/qsG90YkxgF+o525F67z/Ar0uCIoz6UgebnfFX44dfr3k37tlVwgEKtHLIZhxpUddmhh10jz6" +
        "LmMpOsumJtnBUxANBuvhbXwVvssIYxLAaltqYc9DvkgetJQtinc23zZp81zE9D/Sf9lXhKu6oplHsQVURDVC6gZ+ke" +
        "yeiHbI6DM8xf1TV2BjM3V5+C23cyQ9F3fFUM/lMPto9CZyJZTmqRnrckO/dtQ88Q2ESCQXqNOBEf0rL9jlJlWLpK/Z" +
        "LwcTudISL1jW5Nd78IfXr14ejq18wnKYWsYMOq2Sd1u7cBvjt7bTXvkZpb6Lkyqlg2vKNnYcdf3kBSS94fhBFcHKQs" +
        "TSq7F3njQRSsqXO1Tzu8/CBrBqx+/k7aow2DF4Vap0PFN/2/f0WGglroGh6vFk/XF0vH5fZvrDg/Edg1YzNxHuOVJJ" +
        "ZhM3Il11eiQejV2N9V4LPh1O0Sergi1pb+IRlIzCWIA+8Zykjqn97OtF+oxVvIZdXRkIWEa00EmuJuljPCC5pKMMDq" +
        "kixwRw=="
    );

    Services.getDataSource().add(StiODataSource.class);
    Services.getDataBases().add(StiODataDatabase.class);
  }

  private static final String REPORT_CONFIG = "reportConfig";

  private static final Logger log = LoggerFactory.getLogger(ReportService.class);

  private final ClassLoader loader;

  ReportService() {
    this.loader = Thread.currentThread().getContextClassLoader();
  }

  public ReportFront getReport(String reportName) {
    ReportFront reportResult = new ReportFront(reportName);
    try {
      if (reportName.contains("jrxml")) {
        log.info("Report in design mode, build the parameters...");
        InputStream inputStream = this.getInputStream(reportName);
        JasperDesign jasperDesign = JRXmlLoader.load(inputStream);
        Stream.of(jasperDesign.getParameters()).filter(jrParameter -> !jrParameter.isSystemDefined())
            .filter(jrParameter -> !jrParameter.getName().contains("image_"))
            .filter(jrParameter -> !jrParameter.getName().contains("sub_")).forEach(jrParameter -> {
          Parameter parameter = new Parameter();
          parameter.setName(jrParameter.getName());
          parameter.setType(ParameterType.toType(jrParameter.getValueClass()));
          parameter.setDescription(jrParameter.getDescription());
          JRExpression expression = jrParameter.getDefaultValueExpression();
          if (expression != null) {
            parameter.setValue(expression.getText());
          }
          reportResult.addParameter(parameter);
        });
      }
    } catch (JRException e) {
      log.error("Problems to make JasperDesign object.");
      throw new RuntimeException(e);
    }
    return reportResult;
  }

  public String getContentReport(String reportName) {
    try (InputStream inputStream = this.getInputStream(reportName)) {
      try (BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream, CronapiConfigurator.ENCODING))) {
          String content = buffer.lines().collect(Collectors.joining("\n"));
          JsonObject json = (JsonObject) new JsonParser().parse(content);
          json.addProperty("reportName", reportName);
        return json.toString();
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public DataSourcesInBand getDataSourcesParams(DataSourcesInBand dataSourcesInBand) {
    dataSourcesInBand.getDatasources().forEach(dsp -> {
      JsonObject currentCustomQuery = null;
      try {
        currentCustomQuery = QueryManager.getQuery(dsp.getCustomId());
      } catch (Exception e) {
        log.error(e.getMessage());
      }
      List<ParamValue> dsParams = getDataSourceParamsFromCustomQuery(currentCustomQuery);
      dsParams.forEach(param -> {
        //Se nao existir nos parametros, adiciona
        Optional<FieldParam> exist = dsp.getFieldParams().stream().filter(fp -> param.getFieldName().equals(fp.getField())).findAny();
        if (!exist.isPresent()) {
          dsp.getFieldParams().add(new FieldParam(param.getFieldName(), param.getFieldName(), "String", ""));
        }
        dsp.getQueryParams().add(new ParamValue(param.getFieldName(), ":" + param.getFieldName()));
      });
      if (dsp.getFieldParams().size() > 0) {
        dataSourcesInBand.setHasParam(true);
      }
    });
    return dataSourcesInBand;
  }

  public String getPDFAsFile(ReportFront reportFront) {
    ReportExport result = this.getReportExport(reportFront);
    if (result == null) {
      return "";
    }
    result.exportReportToPdfFile();
    return DownloadREST.getDownloadUrl(new File(result.getTargetFile()));
  }

  public byte[] getPDF(ReportFront reportFront) {
    byte[] bytes = new byte[0];
    String reportName = reportFront.getReportName();
    if (reportName.contains(".report")) {
        File file = exportReportFile(reportFront, "pdf");
        if (file.exists()) {
            try {
                bytes = Files.readAllBytes(file.toPath());
                file.delete();
                log.info("Temporary report file removed.");
            } catch (IOException io) {
                log.error("Problems to make the temporary report file.");
                throw new RuntimeException(io);
            }
        }
    } else {
       ReportExport result = this.getReportExport(reportFront);
       if (result == null) {
           return new byte[0];
       } else  {
           bytes = result.toPDF();
        }
    }
    return bytes;
  }

  ReportExport getReportExport(ReportFront reportFront, File file) {
    ReportExport result = null;
    File pdf;
    try {
      if (file == null) {
        pdf = DownloadREST.getTempFile(UUID.randomUUID().toString() + ".pdf");
        if (pdf.createNewFile()) {
          log.info("Temporary report file created.");
        }
      } else {
        pdf = file;
      }
    } catch (IOException e) {
      log.error("Problems to make the temporary report file.");
      throw new RuntimeException(e);
    }

    InputStream inputStream = this.getInputStream(reportFront.getReportName());

    String reportName = reportFront.getReportName();
    if (reportName.contains("jrxml")) {
      ReportManager reportManager = ReportManager.newPrint(inputStream, pdf.getAbsolutePath());
      PrintDesign printDesign = reportManager.byDesign(reportFront.getParameters()).updateParameters()
          .updateImages().updateSubreports();

      J4CDataset dataset = printDesign.getCollectionDataset();
      if (dataset == null) {
        String datasource = printDesign.getDatasource();
        try (Connection connection = this.getConnection(datasource)) {
          result = printDesign.print(connection);
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      } else {
        J4CEntity entity = dataset.getEntity();
        String jpql = entity.getJpql();
        if (Functions.isExists(jpql)) {
          String persistenceUnit = dataset.getPersistenceUnitName();
          EntityManager entityManager = this.getEntityManager(persistenceUnit);

          Map<String, Object> printParameters = printDesign.getPrintParameters();

          Query queryObject = entityManager.createQuery(jpql);

          Set<javax.persistence.Parameter<?>> objectParameters = queryObject.getParameters();
          Set<String> parameterNames = objectParameters.stream().map(javax.persistence.Parameter::getName)
              .collect(Collectors.toSet());

          Set<Map.Entry<String, Object>> entrySet = printParameters.entrySet();
          for (Map.Entry<String, Object> item : entrySet) {
            String name = item.getKey();
            if (parameterNames.contains(name)) {
              Object value = item.getValue();
              queryObject.setParameter(name, value);
            }
          }

          List resultList = Collections.emptyList();
          try {
            resultList = queryObject.getResultList();
          } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
          }
          result = printDesign.print(resultList);
        }
      }
    }
    return result;
  }

  private static String getIfIsDate(String value) {
    Calendar c = Utils.toGenericCalendar(value);
    if (c != null) {
      return "datetimeoffset'" + Utils.getISODateFormat().format((c).getTime()) + "'";
    }
    return null;
  }

  private static String parseParameter(String s, Map<String, String> values) {
    if (s.startsWith(":")) {
      String parameterName = s.substring(1);

      if (values.containsKey(parameterName)) {
        String value = values.get(parameterName);
        String date = getIfIsDate(value);
        if (StringUtils.isNotEmpty(date))
          return date;
        return "'" + StringEscapeUtils.escapeEcmaScript(value) + "'";
      }

      return "''";
    }
    return s;
  }

  private static String getOperatorODATA (String left, String operator, String right) {
    switch (operator) {
      case "%":
        return "substringof(" + right + ", " + left + ")";
      case "=":
        return left + " eq " + right;
      case "!=":
        return left + " ne " + right;
      case ">":
        return left + " gt " + right;
      case ">=":
        return left + " ge " + right;
      case "<":
        return left + " lt " + right;
      case "<=":
        return left + " le " + right;
      default:
        throw new CronapiException("Invalid filter expression type " + operator);
    }
  }

  private static String toFilter (JsonObject data, Map<String, String> values) {

    String result = "";
    if (data != null) {
      JsonArray expressionArgs = data.getAsJsonArray("args");
      JsonPrimitive expressionType = data.getAsJsonPrimitive("type");

      JsonArray args = expressionArgs == null ? new JsonArray() : expressionArgs;
      String type = expressionType == null ? "" : expressionType.getAsString();

      if (args != null && args.size() > 0) {
        for (int i = 0; i < args.size(); i++) {
          JsonObject arg = args.get(i).getAsJsonObject();
          String oper = type;
          if (i == 0) {
            oper = "";
          }

          JsonPrimitive expressionCurrentType = arg.getAsJsonPrimitive("type");
          JsonPrimitive expressionLeft = arg.getAsJsonPrimitive("left");
          JsonPrimitive expressionRight = arg.getAsJsonPrimitive("right");

          String left = expressionLeft == null ? "" : parseParameter(expressionLeft.getAsString(), values);
          String right = expressionRight == null ? "" : parseParameter(expressionRight.getAsString(), values);
          String currentType = expressionCurrentType == null ? "" : expressionCurrentType.getAsString();

          if (arg.getAsJsonArray("args") != null && arg.getAsJsonArray("args").size() > 0) {
            result = result + " " + oper.toLowerCase() + " ( " + toFilter(arg, values) + " ) ";
          } else {
            result = result + " " + oper.toLowerCase() + " " + getOperatorODATA(left, currentType, right);
          }
        }
      }
    }
    return result.trim();
  }

  private static String addParams(String filter, Map<String, String> values) {
    if (values != null) {
      for (Map.Entry<String, String> entry : values.entrySet()) {
        String key = entry.getKey();
        if (!filter.toLowerCase().contains(key.toLowerCase())) {
          filter += (filter.contains("?") ? "&"  : "?") + key + "=" + entry.getValue();
        }
      }
    }
    return filter;
  }

  private static String bindParameters(String query, Map<String, String> values) {
    final String SERVICE_ROOT_URI = "https://localhost/";
    String[] querySlices = query.split("\\?", 2);
    JsonParser jsonParser = new JsonParser();
    JsonObject queryJson = querySlices.length > 1 ? (JsonObject) jsonParser.parse(querySlices[1]) : null;
    JsonObject expressionJson = queryJson != null ? queryJson.getAsJsonObject("expression") : null;
    String filter = toFilter(expressionJson, values);
    String queryString = query;
    if (StringUtils.isNotEmpty(filter)) {
      URIBuilder uriBuilder = ODataClient.newInstance().uriBuilder(SERVICE_ROOT_URI);
      uriBuilder.appendEntitySetSegment(querySlices[0]);
      uriBuilder.filter(filter);
      queryString = uriBuilder.build().toString().replaceFirst(SERVICE_ROOT_URI, "");
    }
    queryString = addParams(queryString, values);
    return queryString;
  }

  public static void main(String[] args) throws Exception {
    String query = "User?{\"params\":[],\"expression\":{\"type\":\"AND\",\"args\":[{\"type\":\"=\",\"left\":\"userName\",\"right\":\":userName\",\"args\":[]}]}}";
    Map<String, String> values = new HashMap<>();
    values.put("userName", "admin");
    System.out.println(bindParameters(query, values));
  }

  /**
   * TODO adicionar mais um parametro: params, para passar para o datasource do report,
   * de acordo com o StimulsoftHelper
   */

  void exportStimulsoftReportToFile(String reportName, File file, Map<String, String> parameters, String type) {
      StiReport stiReport = null;
      try {
          try (InputStream inputStream = this.getInputStream(reportName)) {
              stiReport = StiSerializeManager.deserializeReport(inputStream);
          }

          stiReport.getDataSources().forEach(stiDataSource -> {
              if (stiDataSource instanceof StiODataSource) {
                  StiODataSource stiODataSource = (StiODataSource) stiDataSource;
                  String query = bindParameters(stiODataSource.getQuery(), parameters);
                  stiODataSource.setQuery(query);
              }
          });

          stiReport.Render();

          try (OutputStream outputStream = new FileOutputStream(file)) {

              if ("pdf".equals(type)) {
                StiPdfExportSettings pdfExportSettings = new StiPdfExportSettings();
                pdfExportSettings.setPdfACompliance(true);
                pdfExportSettings.setEmbeddedFonts(true);
                pdfExportSettings.setStandardPdfFonts(true);
                pdfExportSettings.setCompressed(true);
                StiExportManager.exportPdf(stiReport, pdfExportSettings, outputStream);
              } else if ("html".equals(type)) {
                  StiHtmlExportSettings htmlExportSettings = new  StiHtmlExportSettings();
                  htmlExportSettings.setEncoding(Charset.defaultCharset());
                  htmlExportSettings.setExportQuality(StiHtmlExportQuality.High);
                  StiExportManager.exportHtml(stiReport, htmlExportSettings, outputStream);
              }
          }

      } catch (IOException | SAXException | StiDeserializationException | StiException e) {
          log.error("Problems exporting stimulsoft report to pdf file.");
          throw new RuntimeException(e);
      } finally {
          if (stiReport != null) {
              stiReport.dispose();
          }
      }
  }

  void exportStimulsoftReportToPdfFile(String reportName, File file, Map<String, String> parameters) {
      exportStimulsoftReportToFile(reportName,file,parameters, "pdf");
  }

  void exportStimulsoftReportToHtmlFile(String reportName, File file, Map<String, String> parameters) {
      exportStimulsoftReportToFile(reportName,file,parameters, "html");
  }

  private ReportExport getReportExport(ReportFront reportFront) {
    return this.getReportExport(reportFront, null);
  }

  private List<ParamValue> getDataSourceParamsFromCustomQuery(JsonObject customQuery) {
    List<ParamValue> params = new ArrayList<>();
    if (customQuery != null) {
      for (JsonElement queryParamsValues : customQuery.get("queryParamsValues").getAsJsonArray()) {
        JsonObject paramNameValue = queryParamsValues.getAsJsonObject();
        ParamValue paramValue = new ParamValue();
        paramValue.setFieldName(paramNameValue.get("fieldName").getAsString());
        params.add(paramValue);
      }
    }
    return params;
  }

  private EntityManager getEntityManager(String persistenceUnit) {
    HashMap<String, Object> properties = new HashMap<>();
    properties.put(PersistenceUnitProperties.JTA_DATASOURCE, persistenceUnit);
    EntityManagerFactory managerFactory = Persistence.createEntityManagerFactory(persistenceUnit, properties);
    return managerFactory.createEntityManager();
  }

  private Connection getConnection(String datasource) {
    if (datasource != null && !datasource.isEmpty() && !"null".equals(datasource)) {
      javax.naming.Context context = null;
      DataSource dataSource = null;
      try {
        context = (javax.naming.Context) new InitialContext().lookup("java:/comp/env");
        dataSource = (DataSource) context.lookup(datasource);
      } catch (NamingException e) {
        try {
          if (context != null) {
            dataSource = (DataSource) context.lookup(datasource.toLowerCase());
          }
        } catch (NamingException e1) {
          throw new RuntimeException(
              new Exception("Connection context not found.\nError: " + e.getMessage()));
        }
      }
      try {
        if (dataSource != null) {
          return dataSource.getConnection();
        }
      } catch (SQLException e) {
        throw new RuntimeException(
            new Exception("Trouble getting a connection from the context.\nError: " + e.getMessage()));
      }
    }
    return null;
  }

  private InputStream getInputStream(String reportName) {
    InputStream inputStream = loader.getResourceAsStream(reportName);
    if (inputStream == null) {
      throw new RuntimeException("File [" + reportName + "] not found.");
    }
    return inputStream;
  }

  private JsonObject parseJsonObject(String content) {
    return (JsonObject) new JsonParser().parse(content);
  }

  public String getRenderType(String content)
  {
    String type = "PDF";
    JsonObject json = parseJsonObject(content);
    if (json.has(REPORT_CONFIG)) {
        type = json.get(REPORT_CONFIG).getAsString();
    }

    return type;
  }

  private File exportReportFile(ReportFront report, String extension){
    File file = null;
    try {
        file = DownloadREST.getTempFile(UUID.randomUUID().toString() + "." + extension);
        if (file.createNewFile())
            log.info("Temporary report file created.");
        Map<String, String> parameters = new HashMap<>();
        for (Parameter param : report.getParameters())
            parameters.put(param.getName(), param.getValue().toString());
        if ("pdf".equals(extension))
            exportStimulsoftReportToPdfFile(report.getReportName(), file, parameters);
        if ("html".equals(extension)) {
            exportStimulsoftReportToHtmlFile(report.getReportName(), file, parameters);
        }
    } catch (IOException e) {
        log.error("Problems to make the temporary report file.");
        throw new RuntimeException(e);
    }

    return file;
  }

}