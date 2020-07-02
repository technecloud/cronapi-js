package cronapi.report;

import com.sun.xml.bind.v2.TODO;
import cronapi.CronapiMetaData;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;
import cronapi.ParamMetaData;
import cronapi.Var;
import cronapp.reports.ReportExport;
import cronapp.reports.commons.Parameter;
import cronapp.reports.commons.ParameterType;
import cronapp.reports.commons.ReportFront;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe que representa ...
 *
 * @author Usuário de Teste
 * @version 1.0
 * @since 2017-12-04
 */

@CronapiMetaData(category = CategoryType.UTIL, categoryTags = {"Report", "Relatório"})
public class Operations {

  /**
   * Construtor
   **/
  public Operations() {

  }

  @CronapiMetaData(type = "function", name = "{{generateReport}}", nameTags = {"generateReport",
      "GerarRelatorio"}, description = "{{generateReportDescription}}", returnType = ObjectType.OBJECT, wizard = "procedures_generatereport_callreturn")
  public static final Var generateReport(@ParamMetaData(blockType = "util_report_list", type = ObjectType.STRING, description = "{{report}}") Var reportName,
                                         @ParamMetaData(type = ObjectType.STRING, description = "{{path}}") Var path) {
    return cronapi.report.Operations.generateReport(reportName, path, Var.VAR_NULL);
  }

  public static final Var generateReport(Var reportName, Var path, Var params) {
    File file;
    if (!reportName.isNull() || !path.isNull()) {
      ReportService service = new ReportService();
      if (reportName.getObjectAsString().endsWith(".report")) {
        file = new File(path.getObjectAsString());
        /* TODO Passar parâmetros para o DataSource do StimulSoft
          (Precisa implementar essa funcionalidade baseado no StimulsoftHelper) */
        Map<String, String> parameters = new HashMap<>();
        for (Object param : params.getObjectAsList()) {
          parameters.put(Var.valueOf(param).getId(), Var.valueOf(param).getObjectAsString());
        }
        service.exportStimulsoftReportToPdfFile(reportName.getObjectAsString(), file, parameters);
      } else {
        ReportFront reportFront = service.getReport(reportName.getObjectAsString());
        if (params != Var.VAR_NULL && params.size() > 0) {
          for (Object param : params.getObjectAsList()) {
            Parameter parameter = new Parameter();
            parameter.setName(Var.valueOf(param).getId());
            parameter.setType(ParameterType.toType(Var.valueOf(param).getObject().getClass()));
            parameter.setValue(Var.valueOf(param).getObjectAsString());
            reportFront.addParameter(parameter);
          }
        }
        file = new File(path.getObjectAsString());
        ReportExport export = service.getReportExport(reportFront, file);
        if (export != null) {
          export.exportReportToPdfFile();
        } else {
          throw new RuntimeException("Error while exporting report [" + reportName.getObjectAsString() + "]");
        }
      }
    } else {
      throw new RuntimeException("Error without parameters");
    }
    return Var.valueOf(file);
  }
}