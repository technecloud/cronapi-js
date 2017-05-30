package cronapi;

import java.util.Map;

public class RestBody {
  private Var[] inputs;
  private Map<String, Var> fields;
  
  public Var[] getInputs() {
    return inputs;
  }
  
  public void setInputs(Var[] inputs) {
    this.inputs = inputs;
  }
  
  public Map<String, Var> getFields() {
    return fields;
  }
  
  public void setFields(Map<String, Var> fields) {
    this.fields = fields;
  }
}
