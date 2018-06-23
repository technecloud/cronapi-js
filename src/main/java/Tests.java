import cronapi.Var;
import cronapi.util.Operations;

import java.util.Base64;

public class Tests {

  public static void main(String args[]) throws Exception {


    Var urlAddress = Var.valueOf("https://fabrica2.lyceum.com.br/api/financeiro/codPessoaLogada/274627/codAluno/1410001/codBoleto/186342/exibirArquivo/true/obterBoleto");

    String header = "Basic " + new String(Base64.getEncoder().encode(("apiuser:apiuser@123").getBytes()));

    Var result = cronapi.util.Operations.getURLFromOthers(
        Var.valueOf("DELETE"),
        Var.valueOf("application/json"),
        Var.valueOf("http://localhost:7070/api/cronapi/odata/v2/novo/tenant('049CF721-A84E-4AF4-8646-2B8B742F2713')"),
        Var.VAR_NULL,
        Var.VAR_NULL

    );

    System.out.println(result);

  }
}
