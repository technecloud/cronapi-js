import cronapi.Var;
import cronapi.util.Operations;

public class Tests {

  public static void main(String args[]) throws Exception {


    Var urlAddress = Var.valueOf("https://apiuser:apiuser%40123@fabrica2.lyceum.com.br/api/financeiro/codPessoaLogada/274627/codAluno/1410001/codBoleto/186342/exibirArquivo/false/obterBoleto");
    Var path = Var.valueOf("d:\\teste.pdf");

    cronapi.io.Operations.downloadUrltoFile(urlAddress, path);

  }
}
