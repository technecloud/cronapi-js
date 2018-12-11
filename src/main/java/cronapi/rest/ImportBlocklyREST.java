package cronapi.rest;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;
import cronapi.TranslationManager;
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

  private void fill(String base, File folder, List<String> imports) {
    for(File file : folder.listFiles(  (d,s) ->{
   /*     var Teste = new Object() {
          boolean test = false;
        };*/
       boolean valid = true;
        for(String api : API_PATHS){
            if(d.getName().contains(api)){
                valid = false;
                //Teste.test = false;
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
    for(File file : folder.listFiles(  (d,s) ->{
      boolean valid = true;
      for(String api : API_PATHS){
        if(d.getName().equals("i18n")){
          valid = false;
          break;
        }
      }
      return valid;
    } )) {
      if(file.getName().startsWith("i18n")) {
        for(File filess : file.listFiles(  (d,s) ->{
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
    }
    JsonObject localesJson = TranslationManager.getJSON();
    out.println("window.blockly = window.blockly || {};");
    out.println("window.blockly.js = window.blockly.js || {};");
    out.println("window.blockly.js.blockly = window.blockly.js.blockly || {};");
    out.println("window.translations = window.translations || {};");
    out.println("window.translations.locales = {\n" + "    'pt_br': 'Portugues (Brasil)',\n" + "    'en_us': 'English'\n" + "};");
    out.println("window.translations.localesKeys = ['pt_br', 'en_us'];");
    out.println("window.translations.localesRef = {\n" + "    'en*': 'en_us',\n" + "    'pt*': 'pt_br',\n" + "    '*': 'pt_br'\n" + "};");

    for(String js : imports) {
      out.println("document.write(\"<script src='" + js + "'></script>\")");
    }
  }

}