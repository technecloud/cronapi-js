package cronapi;

import javax.xml.bind.annotation.XmlElement;

public class VarElement {
  @XmlElement
  public String value;

  private VarElement() {
  }

  public VarElement(String value) {
    this.value = null;
  }
}
