package cronapi.report;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class DataSourcesInBand implements Serializable {
  private boolean hasParam;
  private List<DataSource> datasources = new LinkedList<>();

  public DataSourcesInBand() {

  }
  public boolean getHasParam() { return this.hasParam; }
  public void setHasParam(boolean hasParam) { this.hasParam = hasParam; }
  public List<DataSource> getDatasources() { return this.datasources; }
  public void setDatasources(List<DataSource> datasources) { this.datasources = datasources; }

  public static class DataSource implements  Serializable {
    private String name;
    private String customId;
    private List<FieldParam> fieldParams = new LinkedList<>();
    private List<ParamValue> queryParams = new LinkedList<>();

    public DataSource() {

    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getCustomId() {
      return customId;
    }

    public void setCustomId(String customId) {
      this.customId = customId;
    }

    public List<FieldParam> getFieldParams() {
      return fieldParams;
    }

    public void setFieldParams(List<FieldParam> fieldParams) {
      this.fieldParams = fieldParams;
    }

    public List<ParamValue> getQueryParams() {
      return queryParams;
    }

    public void setQueryParams(List<ParamValue> queryParams) {
      this.queryParams = queryParams;
    }
  }

  public static class FieldParam implements Serializable {
    private String field;
    private String param;
    private String type;
    private String value;

    public FieldParam() {
    }

    public FieldParam(String field, String param, String type, String value) {
      this.field = field;
      this.param = param;
      this.type = type;
      this.value = value;
    }

    public void setField(String field) {
      this.field = field;
    }

    public void setParam(String param) {
      this.param = param;
    }

    public void setType(String type) {
      this.type = type;
    }

    public void setValue(String value) {
      this.value = value;
    }

    public String getField() {
      return field;
    }


    public String getParam() {
      return param;
    }


    public String getType() {
      return type;
    }

    public String getValue() {
      return value;
    }

  }

  public static class ParamValue implements Serializable {
    private String fieldName;
    private String fieldValue;

    public ParamValue() {

    }

    public void setFieldValue(String fieldValue) {
      this.fieldValue = fieldValue;
    }

    public void setFieldName(String fieldName) {
      this.fieldName = fieldName;
    }

    public ParamValue(String fieldName, String fieldValue) {
      this.fieldName = fieldName;
      this.fieldValue = fieldValue;
    }

    public String getFieldName() {
      return fieldName;
    }

    public String getFieldValue() {
      return fieldValue;
    }

  }

}



