package cronapi.odata.server;

import com.google.gson.Gson;
import cronapi.AppConfig;
import cronapi.QueryManager;
import cronapi.RestClient;
import cronapi.database.DataSource;
import cronapi.rest.DataSourceMapREST;
import cronapi.util.Operations;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.commons.ODataHttpMethod;
import org.apache.olingo.odata2.api.processor.ODataRequest;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.uri.PathSegment;
import org.apache.olingo.odata2.core.ODataContextImpl;
import org.apache.olingo.odata2.core.ODataPathSegmentImpl;
import org.apache.olingo.odata2.core.ODataRequestHandler;
import org.apache.olingo.odata2.core.PathInfoImpl;
import org.apache.olingo.odata2.core.servlet.RestUtil;
import org.eclipse.persistence.internal.jpa.deployment.PersistenceUnitProcessor;
import org.eclipse.persistence.internal.jpa.deployment.SEPersistenceUnitInfo;
import org.eclipse.persistence.internal.jpa.metamodel.EntityTypeImpl;
import org.eclipse.persistence.jpa.Archive;
import org.springframework.data.domain.PageRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.URI;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;

public class ODataAgent {

  private static final String ERROR_TEMPLATE = "<?xml version=\"1.0\" ?><error xmlns=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\"><code></code><message xml:lang=\"en\">{0}</message></error>";

  private static final int START_RESULT = 0x1C;
  private static final int END_RESULT = 0x1D;

  private static final int DEFAULT_BUFFER_SIZE = 32768;
  private static final String DEFAULT_READ_CHARSET = "utf-8";

  private static void bind(File contextFile, String activeProfile) throws Exception {
    System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "cronapi.osjava.sj.memory.MemoryContextFactory");
    System.setProperty("org.osjava.sj.jndi.shared", "true");

    Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(contextFile);
    XPath xpath = XPathFactory.newInstance().newXPath();
    NodeList nodes = (NodeList) xpath.evaluate("//Resource", doc, XPathConstants.NODESET);

    Set<String> added = new HashSet<>();

    for (int i = 0; i < nodes.getLength(); i++) {
      Node elem = nodes.item(i);
      String name = elem.getAttributes().getNamedItem("name").getTextContent();
      String profile = elem.getAttributes().getNamedItem("profile").getTextContent();

      if (activeProfile.equalsIgnoreCase(profile) && !added.contains(name)) {
        final BasicDataSource ds = createDataSource(elem);

        final Context context = new InitialContext();
        context.bind("java:comp/env/" + name, ds);

        added.add(name);
      }
    }

    for (int i = 0; i < nodes.getLength(); i++) {
      Node elem = nodes.item(i);
      String name = elem.getAttributes().getNamedItem("name").getTextContent();

      if (!added.contains(name)) {
        final BasicDataSource ds = createDataSource(elem);

        final Context context = new InitialContext();
        context.bind("java:comp/env/" + name, ds);

        added.add(name);
      }
    }
  }

  private static BasicDataSource createDataSource(final Node elem) {
    final BasicDataSource ds = new BasicDataSource();
    ds.setUrl(elem.getAttributes().getNamedItem("url").getTextContent());
    ds.setDriverClassName(elem.getAttributes().getNamedItem("driverClassName").getTextContent());
    ds.setUsername(elem.getAttributes().getNamedItem("username").getTextContent());
    ds.setPassword(elem.getAttributes().getNamedItem("password").getTextContent());
    return ds;
  }

  private static String xmlEscapeText(String t) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < t.length(); i++) {
      char c = t.charAt(i);
      switch (c) {
        case '<':
          sb.append("&lt;");
          break;
        case '>':
          sb.append("&gt;");
          break;
        case '\"':
          sb.append("&quot;");
          break;
        case '&':
          sb.append("&amp;");
          break;
        case '\'':
          sb.append("&apos;");
          break;
        default:
          if (c > 0x7e) {
            sb.append("&#").append((int) c).append(";");
          } else {
            sb.append(c);
          }
      }
    }
    return sb.toString();
  }

  private static void sendError(String msg) {
    System.out.println();
    System.out.write(START_RESULT);
    System.out.print(MessageFormat.format(ERROR_TEMPLATE, xmlEscapeText(msg)));
    System.out.write(END_RESULT);
    System.out.println();
  }

  private static List<SEPersistenceUnitInfo> getPersistenceUnits(Archive archive) {
    return PersistenceUnitProcessor.getPersistenceUnits(archive, Thread.currentThread().getContextClassLoader());
  }

  private static EntityManagerFactory find(String pu) {
    Set<Archive> archives = PersistenceUnitProcessor.findPersistenceArchives();

    for (Archive archive : archives) {
      List<SEPersistenceUnitInfo> persistenceUnitInfos = getPersistenceUnits(archive);

      for (SEPersistenceUnitInfo pui : persistenceUnitInfos) {
        String namespace = pui.getPersistenceUnitName();

        if (pu == null || namespace.equalsIgnoreCase(pu)) {
          Properties properties = pui.getProperties();
          properties.setProperty("eclipselink.ddl-generation", "none");

          return Persistence.createEntityManagerFactory(namespace, properties);
        }
      }
    }

    return null;
  }

  private static synchronized void odata(String strPath) {
    try {
      String queryString = null;

      if (strPath.contains("?")) {
        String[] urlParts = strPath.split("\\?");
        queryString = urlParts[1];
        strPath = urlParts[0];

        RestClient.getRestClient().setParameters(queryString);
      } else {
        RestClient.getRestClient().setParameters("");
      }

      String[] parts = strPath.split("/");
      String pu = parts[0];

      Set<Archive> archives = PersistenceUnitProcessor.findPersistenceArchives();

      boolean found = false;

      int idx = 0;
      for (Archive archive : archives) {
        List<SEPersistenceUnitInfo> persistenceUnitInfos = getPersistenceUnits(archive);

        for (SEPersistenceUnitInfo pui : persistenceUnitInfos) {
          String namespace = pui.getPersistenceUnitName();

          if (pu == null || namespace.equalsIgnoreCase(pu)) {
            found = true;
            Properties properties = pui.getProperties();
            properties.setProperty("eclipselink.ddl-generation", "none");

            EntityManagerFactory emf = Persistence.createEntityManagerFactory(namespace, properties);
            JPAODataServiceFactory serviceFactory = new JPAODataServiceFactory(emf, namespace, idx);

            idx++;

            List<PathSegment> odataPathSegment = new LinkedList<>();
            for (int i = 1; i < parts.length; i++) {
              odataPathSegment.add(new ODataPathSegmentImpl(parts[i], new LinkedHashMap<>()));
            }
            PathInfoImpl path = new PathInfoImpl();
            path.setODataPathSegment(odataPathSegment);
            path.setServiceRoot(new URI("file:///local/"));
            path.setRequestUri(new URI("file:///local/" + strPath));

            InputStream ip = new ByteArrayInputStream(new byte[0]);

            ODataRequest odataRequest = ODataRequest.method(ODataHttpMethod.GET).httpMethod("GET")
                .contentType(RestUtil.extractRequestContentType(null).toContentTypeString())
                .acceptHeaders(RestUtil.extractAcceptHeaders(
                    "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"))
                .acceptableLanguages(RestUtil.extractAcceptableLanguage("en-US")).pathInfo(path)
                .allQueryParameters(RestUtil.extractAllQueryParameters(queryString, null))
                .requestHeaders(new HashMap<>()).body(ip).build();

            ODataContextImpl context = new ODataContextImpl(odataRequest, serviceFactory);
            ODataService service = serviceFactory.createService(context);
            context.setService(service);
            service.getProcessor().setContext(context);

            String jpql = RestClient.getRestClient().getParameter("jpql");

            if (jpql != null && !jpql.isEmpty()) {
              ((DatasourceExtension) serviceFactory.getODataJPAContext().getJPAEdmExtension()).jpql(jpql);
            }

            ODataRequestHandler requestHandler = new ODataRequestHandler(serviceFactory, service, context);
            final ODataResponse odataResponse = requestHandler.handle(odataRequest);

            Object entity = odataResponse.getEntity();
            System.out.println();
            System.out.write(START_RESULT);

            if (entity != null) {
              if (entity instanceof InputStream) {
                handleStream((InputStream) entity);
              } else if (entity instanceof String) {
                String body = (String) entity;
                final byte[] entityBytes = body.getBytes(DEFAULT_READ_CHARSET);
                System.out.write(entityBytes);
              } else {
                System.out.print("Illegal entity object in ODataResponse of type '" + entity.getClass() + "'");
              }
            }
            System.out.write(END_RESULT);
            System.out.println();
          }
        }
      }

      if (!found) {
        sendError("No persistence provided found!");
      }
    } catch (Exception e) {
      sendError(e.getMessage());
    }
  }

  private static void handleStream(InputStream stream) throws IOException {
    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

    try (InputStream closeableStream = stream) {
      int len;
      while ((len = closeableStream.read(buffer)) != -1) {
        System.out.write(buffer, 0, len);
      }
    }
  }

  private static synchronized void datasourceMap() {
    try {
      System.out.println();
      System.out.write(START_RESULT);
      DataSourceMapREST.cleanCache();
      DataSourceMapREST ds = new DataSourceMapREST();
      StringWriter writer = new StringWriter();
      ds.writeMap(writer);
      System.out.print(writer.toString());
      System.out.write(END_RESULT);
      System.out.println();
    } catch (Exception e) {
      sendError(e.getMessage());
    }
  }

  private static synchronized void datasource(String strPath) {
    try {
      String queryString;

      if (strPath.contains("?")) {
        String[] urlParts = strPath.split("\\?");
        strPath = urlParts[0];
        queryString = urlParts[1];
        RestClient.getRestClient().setParameters(queryString);
      } else {
        RestClient.getRestClient().setParameters("");
      }

      String[] parts = strPath.split("/");
      String pu = parts[0];

      EntityManagerFactory factory = find(pu);
      EntityManager entityManager = Objects.requireNonNull(factory).createEntityManager();
      PageRequest p = PageRequest.of(RestClient.getRestClient().getParameterAsInt("$skip", 0), RestClient.getRestClient().getParameterAsInt("$top", 100));

      DataSource ds = new DataSource(((EntityTypeImpl) entityManager.getMetamodel().getEntities().toArray()[0]).getJavaTypeName(), entityManager);
      ds.disableMultiTenant();
      ds.setUseUrlParams(true);
      ds.setPlainData(RestClient.getRestClient().getParameterAsBoolean("plain", true));

      ds.filter(RestClient.getRestClient().getParameter("jpql"), p);

      System.out.println();
      System.out.write(START_RESULT);
      Gson gson = new Gson();
      System.out.print(gson.toJson(ds.getPage().getContent()));

      System.out.write(END_RESULT);
      System.out.println();
    } catch (Exception e) {
      sendError(e.getMessage());
    }
  }

  public static void main(String[] args) throws Exception {
    QueryManager.DISABLE_AUTH = true;
    AppConfig.FORCE_METADATA = true;
    AppConfig.FORCE_LOCAL_ENTITIES = true;
    Operations.IS_DEBUG = true;
    ODataRequestHandler.PRINT_EXCEPTION = false;

    PrintStream errStream = System.err;

    System.setErr(new PrintStream(new OutputStream() {
      @Override
      public void write(int b) {
        //Faz Nada
      }
    }));

    try {
      Class.forName("SpringBootMain");
    } catch (ClassNotFoundException exception) {
      TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    System.out.println("Starting CronApp Data Agent");

    bind(new File(args[0]), args[2]);

    QueryManager.loadJSONFromFile(new File(args[1]));

    Scanner scanner = new Scanner(System.in);
    scanner.useDelimiter(Pattern.compile("[\\n]+"));

    System.out.println("Waiting for commands...");
    while (true) {
      String input = scanner.next();
      if (input.startsWith("error")) {
        System.setErr(errStream);
        ODataRequestHandler.PRINT_EXCEPTION = true;
        System.out.println("Err sent to printstream");
      } else if (input.startsWith("odata ")) {
        odata(input.substring(6).trim());
      } else if (input.startsWith("datasourcemap")) {
        datasourceMap();
      } else if (input.startsWith("ds ")) {
        datasource(input.substring(3).trim());
      } else {
        sendError("Command not found!");
      }
    }
  }
}