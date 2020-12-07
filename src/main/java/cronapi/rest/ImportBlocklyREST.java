package cronapi.rest;

import com.google.gson.JsonObject;
import cronapi.AppConfig;
import cronapi.util.Operations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

@RestController
@RequestMapping(value = "/js/blockly.js")
public class ImportBlocklyREST {

  private static final List<String> API_PATHS = Arrays.asList("cronapi-js", "cronapp-framework-js", "cronapp-framework-mobile-js");
  private static List<String> imports;
  private static volatile boolean shouldInitializeImports = true;
  private static List<String> localesKeys = new ArrayList<>();
  private static JsonObject localesRef = new JsonObject();

  private final Logger logger;

  public ImportBlocklyREST(Optional<Logger> logger) {
    this.logger = logger.orElse(Logger.getLogger(this.getClass().getName()));
  }

  private static boolean isValidApiPath(Path path) {
    String pathString = path.toString();
    return API_PATHS.stream().noneMatch(pathString::contains);
  }

  private static boolean isValidLocalePath(String path) {
    return path.startsWith("locale") && path.endsWith(".json");
  }

  private static void fillLanguages(Path folder) throws IOException {
    localesKeys = new ArrayList<>();
    localesRef = new JsonObject();
    Path i18nPath = folder.resolve("i18n");

    if (!Files.exists(i18nPath)) {
      return;
    }

    try (Stream<Path> stream = Files.walk(i18nPath)) {
      stream
          .map(path -> path.getFileName().toString())
          .filter(ImportBlocklyREST::isValidLocalePath)
          .forEach(ImportBlocklyREST::addLocale);
    }
  }

  private static void addLocale(String path) {
    String localeName = path.substring(7, path.length() - 5);
    fillLanguageSet(localeName);
  }

  private static void fillLanguageSet(String localeName) {
    if (!localesKeys.contains(localeName)) {
      localesKeys.add(localeName);
    }
    localesRef.addProperty(localeName.substring(0, 2) + "*", localeName);
    if (localesRef.get("*") == null) {
      localesRef.addProperty("*", localeName);
    }
    if (localeName.equals("pt_br")) {
      localesRef.addProperty("*", localeName);
    }
  }

  private void fill(Path base, List<String> imports) throws IOException {
    try (Stream<Path> stream = Files.walk(base)) {
      stream
          .filter(ImportBlocklyREST::isValidApiPath)
          .forEach(candidate -> addImport(base, candidate, imports));
    }
  }

  private void addImport(Path base, Path path, List<String> imports) {
    String hash = "";

    try {
      BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);

      if (attributes.isDirectory()) {
        return;
      }

      FileTime lastModifiedTime = attributes.lastModifiedTime();
      if (lastModifiedTime != null) {
        hash = "?" + attributes.lastModifiedTime().toMillis();
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error getting path attributes", e);
    }


    String pathString = base.relativize(path).toString();

    if (pathString.endsWith(".blockly.js")) {
      String js = pathString.replace("\\", "/");
      if (js.startsWith("/")) {
        js = js.substring(1);
      }

      imports.add(js + hash);
    }
  }

  @GetMapping()
  public void listBlockly(HttpServletRequest request, HttpServletResponse response) throws IOException, URISyntaxException {
    response.setContentType("application/javascript");
    PrintWriter out = response.getWriter();
    if (shouldInitializeImports) {
      synchronized (ImportBlocklyREST.class) {
        if (shouldInitializeImports) {
          List<String> fillImports = new ArrayList<>();
          FileSystem fileSystem = null;

          Path folderPath;
          ServletContext servletContext = request.getServletContext();
          String folderRealPath = servletContext.getRealPath("/");

          if (folderRealPath == null) {
            URI folderUri = request.getServletContext().getResource("/").toURI();

            Map<String, String
                > env = new HashMap<>();
            env.put("create", "true");

            fileSystem = FileSystems.newFileSystem(folderUri, env);
            folderPath = Paths.get(folderUri);
          } else {
            folderPath = Paths.get(folderRealPath);
          }

          try {
            fill(folderPath, fillImports);
            if (!Operations.IS_DEBUG) {
              imports = fillImports;
              shouldInitializeImports = false;
            } else {
              fillLanguages(folderPath);
              write(out, fillImports);
            }
          } finally {
            if (fileSystem != null) {
              fileSystem.close();
            }
          }
        }
      }
    }

    if (imports != null) {
      write(out, imports);
    }
  }

  private void write(PrintWriter out, List<String> imports) {
    String localesKeysString = arrayToString(localesKeys) + ";";
    String localesRefString = localesRef.toString() + ";";
    out.println("window.fixedTimeZone = " + AppConfig.fixedTimeZone() + ";");
    out.println("window.timeZone = '" + AppConfig.timeZone() + "';");
    out.println("window.timeZoneOffset = " + AppConfig.timeZoneOffset() + ";");
    out.println("window.blockly = window.blockly || {};");
    out.println("window.blockly.js = window.blockly.js || {};");
    out.println("window.blockly.js.blockly = window.blockly.js.blockly || {};");
    out.println("window.translations = window.translations || {};");
    out.println("window.translations.localesKeys = " + localesKeysString);
    out.println("window.translations.localesRef =  " + localesRefString);

    for (String js : imports) {
      out.println("document.write(\"<script src='" + js + "'></script>\")");
    }
  }

  private String arrayToString(List<String> stringList) {
    StringBuilder b = new StringBuilder();
    b.append("[");
    for (String key : stringList) {
      if (stringList.indexOf(key) != 0) {
        b.append(",");
      }
      b.append("'").append(key).append("'");
    }
    b.append("]");
    return b.toString();
  }
}