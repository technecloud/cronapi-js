package cronapi;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class VarAdapter extends XmlAdapter<String, Var> {

  public String marshal(Var value) {
    if (value != null) {
      return value.getObjectAsString();
    }

    return null;
  }

  public Var unmarshal(String value) {
    if (value != null) {
      return Var.valueOf(value);
    }

    return Var.VAR_NULL;
  }
}
