package cronapi.report.odata;

import com.stimulsoft.base.StiJsonSaveMode;
import com.stimulsoft.base.json.JSONException;
import com.stimulsoft.base.json.JSONObject;
import com.stimulsoft.base.serializing.annotations.StiSerializable;
import com.stimulsoft.report.StiOptions.Dictionary;
import com.stimulsoft.report.dictionary.dataSources.StiDataTableSource;
import com.stimulsoft.report.dictionary.databases.StiDatabase;

public class StiODataSource extends StiDataTableSource {

  private String sqlCommand;

  @StiSerializable
  private String getSqlCommand() {
    if (Dictionary.isIgnoreLastSemicolonQuery() && this.sqlCommand != null && this.sqlCommand.endsWith(";")) {
      return this.sqlCommand.substring(0, this.sqlCommand.length() - 1);
    }
    return this.sqlCommand;
  }

  public String getQuery() {
    return this.getSqlCommand();
  }

  public void setQuery(String sqlCommand) {
    this.sqlCommand = sqlCommand;
  }

  @Override
  public Class<? extends StiDatabase> getDatabaseClass() {
    return StiODataDatabase.class;
  }

  @Override
  public JSONObject SaveToJsonObject(StiJsonSaveMode mode) throws JSONException {
    JSONObject jObject = super.SaveToJsonObject(mode);
    jObject.AddPropertyStringNullOfEmpty("SqlCommand", this.getSqlCommand());
    return jObject;
  }

  @Override
  public void LoadFromJsonObject(JSONObject jObject) throws JSONException {
    super.LoadFromJsonObject(jObject);
    jObject.Properties().stream()
        .filter(property -> property.Name.equals("SqlCommand"))
        .forEach(property -> this.sqlCommand = (String) property.Value);
  }
}