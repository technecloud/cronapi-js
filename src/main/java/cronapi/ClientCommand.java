package cronapi;

import java.util.LinkedList;
import java.util.List;

public class ClientCommand {
  private Var function;
  private List<Var> params = new LinkedList<Var>();
  
  public ClientCommand(String function) {
    this.function = Var.valueOf(function);
  }
  
  public void addParam(Object value) {
    params.add(Var.valueOf(value));
  }
  
  public Var getFunction() {
    return function;
  }
  
  public void setFunction(Var function) {
    this.function = function;
  }
  
  public List<Var> getParams() {
    return params;
  }
  
  public void setParams(List<Var> params) {
    this.params = params;
  }
}
