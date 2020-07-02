package cronapi.report.odata;

import com.stimulsoft.base.StiJsonSaveMode;
import com.stimulsoft.base.exception.StiException;
import com.stimulsoft.base.json.JSONException;
import com.stimulsoft.base.json.JSONObject;
import com.stimulsoft.report.StiReport;
import com.stimulsoft.report.dictionary.data.DataTable;
import com.stimulsoft.report.dictionary.dataSources.StiDataStoreSource;
import com.stimulsoft.report.dictionary.databases.StiDatabase;
import com.stimulsoft.report.dictionary.databases.StiEncryptionConnectionString;

import java.security.NoSuchAlgorithmException;

public class StiODataDatabase extends StiDatabase {

  private static final String ENCRYPTED_ID = "8pTP5X15uKADcSw7";

  private String connectionString;

  private String getConnectionString() {
    return this.connectionString;
  }

  private void setConnectionString(String value) {
    this.connectionString = value;
  }

  private String getConnectionStringEncrypted() throws NoSuchAlgorithmException {
    return (new StiEncryptionConnectionString()).encrypt(this.connectionString, ENCRYPTED_ID);
  }

  private void setConnectionStringEncrypted(String value) throws NoSuchAlgorithmException {
    this.setConnectionString((new StiEncryptionConnectionString()).decrypt(value, ENCRYPTED_ID));
  }

  @Override
  public void connect(StiDataStoreSource source, StiReport report) throws StiException {
    this.connect(source, true, report);
  }

  @Override
  public void connect(StiDataStoreSource source, Boolean fillTable, StiReport report) throws StiException {
    if (!fillTable) {
      return;
    }

    source.disconnect();

    StiODataSource oDataSource = (StiODataSource) source;

    StiODataDatabase connection = report.getDictionary().getDatabases().stream()
        .filter(database -> database != null && database.getName().equals(oDataSource.getNameInSource()))
        .map(database -> (StiODataDatabase) database)
        .findFirst().orElse(null);

    if (connection == null) {
      return;
    }

    DataTable dataTable = oDataSource.createNewTable();

    StiODataHelper oDataHelper = new StiODataHelper(connection.getConnectionString());

    oDataHelper.fillDataTable(dataTable, oDataSource.getQuery());
    oDataSource.setDataTable(dataTable);
  }

  @Override
  public void disconnect() {
  }

  @Override
  public JSONObject SaveToJsonObject(StiJsonSaveMode mode) throws JSONException {
    JSONObject jObject = super.SaveToJsonObject(mode);
    try {
      jObject.AddPropertyStringNullOfEmpty("ConnectionStringEncrypted", this.getConnectionStringEncrypted());
    } catch (NoSuchAlgorithmException ignored) {
    }
    return jObject;
  }

  @Override
  public void LoadFromJsonObject(JSONObject jObject) throws JSONException {
    super.LoadFromJsonObject(jObject);
    jObject.Properties().stream()
        .filter(property -> property.Name.equals("ConnectionStringEncrypted"))
        .forEach(property -> {
          try {
            this.setConnectionStringEncrypted((String) property.Value);
          } catch (NoSuchAlgorithmException ignored) {
          }
        });
  }
}