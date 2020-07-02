package cronapi.report.odata;

import com.stimulsoft.base.exception.StiException;
import com.stimulsoft.report.dictionary.data.DataRow;
import com.stimulsoft.report.dictionary.data.DataTable;
import cronapi.QueryManager;
import cronapi.RestClient;
import cronapi.odata.server.DatasourceExtension;
import cronapi.odata.server.JPAODataServiceFactory;
import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.commons.ODataHttpMethod;
import org.apache.olingo.odata2.api.processor.ODataRequest;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.uri.PathInfo;
import org.apache.olingo.odata2.api.uri.PathSegment;
import org.apache.olingo.odata2.core.ODataContextImpl;
import org.apache.olingo.odata2.core.ODataPathSegmentImpl;
import org.apache.olingo.odata2.core.ODataRequestHandler;
import org.apache.olingo.odata2.core.PathInfoImpl;
import org.apache.olingo.odata2.core.commons.ContentType;
import org.apache.olingo.odata2.core.servlet.RestUtil;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.eclipse.persistence.internal.jpa.deployment.PersistenceUnitProcessor;
import org.eclipse.persistence.internal.jpa.deployment.SEPersistenceUnitInfo;
import org.eclipse.persistence.jpa.Archive;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class StiODataHelper {

  private final String connectionString;

  StiODataHelper(String connectionString) {
    this.connectionString = connectionString.replace("api/cronapi/odata/v2/", "");
  }

  void fillDataTable(DataTable dataTable, String query) throws StiException {
    if (StringUtils.isEmpty(connectionString)) {
      return;
    }

    String url = StiUrl.combine(new String[]{this.connectionString, query});

    String metadata = downloadODataString(url);

    if (StringUtils.isEmpty(metadata)) {
      return;
    }

    fillXmlDataTable(dataTable, metadata);
  }

  private void fillXmlDataTable(DataTable dataTable, String metadata) throws StiException {
    try {
      DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = documentBuilder.parse(new InputSource(new StringReader(metadata)));

      NodeList title = document.getElementsByTagName("title");

      if (title != null && title.getLength() > 0) {
        dataTable.setTableName(title.item(0).getTextContent());
      }

      NodeList entries = document.getElementsByTagName("entry");

      for (Node entry : nodeListToList(entries)) {
        Node elementContent = getContentNode(entry.getChildNodes());

        if (elementContent == null) {
          continue;
        }

        Node elementProperties = getPropertiesNode(elementContent.getChildNodes());

        if (elementProperties == null) {
          continue;
        }

        DataRow row = dataTable.NewRow();
        dataTable.addRow(row);

        Node elementTitle = entry.getAttributes().getNamedItem("title");
        if (elementTitle != null && dataTable.existColumn("Name")) {
          row.setValue("Name", elementTitle.getTextContent());
        }

        Node elementSummary = entry.getAttributes().getNamedItem("summary");
        if (elementSummary != null && dataTable.existColumn("Description")) {
          row.setValue("Description", elementSummary.getTextContent());
        }

        for (Node elementProperty : nodeListToList(elementProperties.getChildNodes())) {
          String columnName = elementProperty.getNodeName().replaceAll("d:", "");
          String columnTextValue = elementProperty.getTextContent();

          if (dataTable.existColumn(columnName)) {
            row.setValue(columnName, columnTextValue);
          }
        }
      }
    } catch (Exception e) {
      throw new StiException(e);
    }
  }

  private Node getContentNode(NodeList collection) {
    return nodeListToList(collection).stream()
        .filter(node -> node.getNodeName().equals("content"))
        .findFirst().orElse(null);
  }

  private Node getPropertiesNode(NodeList collection) {
    return nodeListToList(collection).stream()
        .filter(node -> node.getNodeName().endsWith("properties"))
        .findFirst().orElse(null);
  }

  private String downloadODataString(String path) throws StiException {
    try {
      String queryString = null;

      if (path.contains("?")) {
        String[] urlParts = path.split("\\?");
        queryString = urlParts[1];
        path = urlParts[0];

        RestClient.getRestClient().setParameters(queryString);
      } else {
        RestClient.getRestClient().setParameters("");
      }

      String[] parts = path.split("/");
      String persistenceUnit = parts[0];

      Set<Archive> archives = PersistenceUnitProcessor.findPersistenceArchives();

      List<SEPersistenceUnitInfo> persistenceUnitInfos = archives.stream()
          .map(archive -> PersistenceUnitProcessor.getPersistenceUnits(archive, Thread.currentThread().getContextClassLoader()))
          .flatMap(Collection::stream)
          .filter(persistenceUnitInfo -> {
            String namespace = persistenceUnitInfo.getPersistenceUnitName();
            return persistenceUnit == null || namespace.equalsIgnoreCase(persistenceUnit);
          })
          .collect(Collectors.toList());

      int order = 0;

      for (SEPersistenceUnitInfo persistenceUnitInfo : persistenceUnitInfos) {
        String namespace = persistenceUnitInfo.getPersistenceUnitName();
        Properties properties = persistenceUnitInfo.getProperties();
        properties.setProperty("eclipselink.ddl-generation", "none");

        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory(namespace, properties);
        JPAODataServiceFactory serviceFactory = new JPAODataServiceFactory(entityManagerFactory, namespace, order);

        order++;

        List<PathSegment> odataPathSegment = new LinkedList<>();

        for (int i = 1; i < parts.length; i++) {
          odataPathSegment.add(new ODataPathSegmentImpl(parts[i], new LinkedHashMap<>()));
        }

        PathInfo pathInfo = buildPathInfo(odataPathSegment);

        InputStream inputStream = new ByteArrayInputStream(new byte[0]);

        ODataRequest odataRequest = buildODataRequest(pathInfo, queryString, inputStream);

        ODataContextImpl context = new ODataContextImpl(odataRequest, serviceFactory);
        ODataService oDataService = serviceFactory.createService(context);
        context.setService(oDataService);
        oDataService.getProcessor().setContext(context);

        setJpqlExtension(serviceFactory);

        ODataRequestHandler oDataRequestHandler = new ODataRequestHandler(serviceFactory, oDataService, context);

        QueryManager.DISABLE_AUTH = true; // TODO tratar autenticação

        final ODataResponse odataResponse = oDataRequestHandler.handle(odataRequest);

        Object entity = odataResponse.getEntity();

        if (entity != null) {
          if (entity instanceof InputStream) {
            return convert((InputStream) entity);
          }
          if (entity instanceof String) {
            return (String) entity;
          }
        }
      }
    } catch (Exception e) {
      throw new StiException(e);
    } finally {
      QueryManager.DISABLE_AUTH = false;
    }

    return null;
  }

  private void setJpqlExtension(JPAODataServiceFactory serviceFactory) throws ODataJPARuntimeException {
    String jpql = RestClient.getRestClient().getParameter("jpql");

    if (jpql != null && !jpql.isEmpty()) {
      ((DatasourceExtension) serviceFactory.getODataJPAContext().getJPAEdmExtension()).jpql(jpql);
    }
  }

  private PathInfo buildPathInfo(List<PathSegment> odataPathSegment) throws URISyntaxException {
    PathInfoImpl pathInfo = new PathInfoImpl();
    pathInfo.setODataPathSegment(odataPathSegment);
    pathInfo.setServiceRoot(new URI("file:///local/"));
    pathInfo.setRequestUri(new URI("file:///local/" + pathInfo));
    return pathInfo;
  }

  private ODataRequest buildODataRequest(PathInfo pathInfo, String queryString, InputStream inputStream) {
    List<String> acceptHeaders = RestUtil.extractAcceptHeaders("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
    Map<String, List<String>> allQueryParameters = RestUtil.extractAllQueryParameters(queryString, null);

    return ODataRequest.method(ODataHttpMethod.GET).httpMethod("GET")
        .contentType(ContentType.APPLICATION_OCTET_STREAM.toContentTypeString())
        .acceptHeaders(acceptHeaders)
        .acceptableLanguages(Collections.singletonList(Locale.US)).pathInfo(pathInfo)
        .allQueryParameters(allQueryParameters)
        .requestHeaders(new HashMap<>()).body(inputStream).build();
  }

  private String convert(InputStream inputStream) throws IOException {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      return reader.lines().collect(Collectors.joining(System.lineSeparator()));
    }
  }

  private List<Node> nodeListToList(NodeList nodeList) {
    return IntStream.range(0, nodeList.getLength())
        .mapToObj(nodeList::item)
        .collect(Collectors.toList());
  }
}