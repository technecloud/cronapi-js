package cronapi.rest;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cronapi.util.Operations;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/js/blockly.js")
public class ImportBlocklyREST {

  private static List<String> imports;
  private static boolean isDebug = Operations.IS_DEBUG;
  private final static List<String> API_PATHS = new ArrayList<String>(){{add("cronapi-js");add("cronapp-framework-js");add("cronapp-framework-mobile-js");}};
  private static JsonObject localesJson = new JsonObject();
  private static List<String> localesKeys = new ArrayList<String>();
  private static JsonObject localesRef  = new JsonObject();

  private void fill(String base, File folder, List<String> imports) {
    for(File file : folder.listFiles(  (d,s) ->{
       boolean valid = true;
        for(String api : API_PATHS){
            if(d.getName().contains(api)){
                valid = false;
                break;
            }
        }
      return valid;
    } )) {
      if(file.isDirectory()) {
        fill(base, file, imports);
      }
      else {
        if(file.getName().endsWith(".blockly.js")) {
          String js = file.getAbsolutePath().replace(base, "");
          js = js.replace("\\", "/");
          if(js.startsWith("/")) {
            js = js.substring(1);
          }
          imports.add(js + "?" + file.lastModified());
        }
      }
    }
  }

  @RequestMapping(method = RequestMethod.GET)
  public void listBlockly(HttpServletRequest request, HttpServletResponse response) throws Exception {
    response.setContentType("application/javascript");
    PrintWriter out = response.getWriter();
    if(imports == null) {
      synchronized(ImportBlocklyREST.class) {
        if(imports == null) {
          List<String> fillImports = new ArrayList<>();
          File folder = new File(request.getServletContext().getRealPath("/"));
          fill(request.getServletContext().getRealPath("/"), folder, fillImports);
          if(!isDebug) {
            imports = fillImports;
          }
          else {
            write(out, fillImports, request.getServletContext().getRealPath("/"), folder);
          }
        }
      }
    }

    if(imports != null) {
      File folder = new File(request.getServletContext().getRealPath("/"));
      write(out, imports, request.getServletContext().getRealPath("/"), folder);
    }
  }

  private void write(PrintWriter out, List<String> imports, String base, File folder) {
    localesJson = new JsonObject();
    localesKeys = new ArrayList<String>();
    localesRef = new JsonObject();
    for(File file : folder.listFiles(  (d,s) ->{
      boolean valid = false;
        if(s.equals("i18n")){
          valid = true;
          return valid;
        }
      return valid;
    } )) {
      if(file.isDirectory()) {
        for(File filee : file.listFiles(  (d,s) ->{
          boolean valid = false;
          if(s.startsWith("locale") && s.endsWith(".json")){
            valid = true;
            return valid;
          }
          return valid;
        } )) {
          String localeName = filee.getName().substring(7,  filee.getName().length() - 5);
          fillLanguageSet(localeName);
        }
      }
    }
    String localesJsonString  = localesJson.toString() + ";";
    String localesKeysString = arrayToString(localesKeys)  + ";";
    String localesRefString = localesRef.toString()  + ";";
    out.println("window.blockly = window.blockly || {};");
    out.println("window.blockly.js = window.blockly.js || {};");
    out.println("window.blockly.js.blockly = window.blockly.js.blockly || {};");
    out.println("window.translations = window.translations || {};");
    out.println("window.translations.locales = " + localesJsonString);
    out.println("window.translations.localesKeys = " + localesKeysString);
    out.println("window.translations.localesRef =  " + localesRefString);

    for(String js : imports) {
      out.println("document.write(\"<script src='" + js + "'></script>\")");
    }
  }

  private void fillLanguageSet(String localeName){
    if(localesJson.get(localeName) == null){
      localesJson.addProperty(localeName, localeName);
    }
    if(localesKeys.indexOf(localeName) == -1){
      localesKeys.add(localeName);
    }
    localesRef.addProperty(localeName.substring(0,2) + "*", localeName);
    if(localesRef.get("*") == null){
      localesRef.addProperty("*", localeName);
    }
    if(localeName.equals("pt_br")){
      localesRef.addProperty("*", localeName);
    }
  }

  private String arrayToString(List<String> stringList){
    StringBuilder b = new StringBuilder();
    b.append("[");
    for(String key : stringList){
      if(stringList.indexOf(key) != 0) {
        b.append(",");
      }
      b.append("'" + key + "'");
    }
    b.append("]");
    return b.toString();
  }
}