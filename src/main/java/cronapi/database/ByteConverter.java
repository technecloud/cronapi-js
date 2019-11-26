package cronapi.database;

import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.sessions.Session;


public class ByteConverter implements Converter {

  @Override
  public Object convertObjectValueToDataValue(Object objectValue, Session session) {
    if (objectValue instanceof byte[]) {
      return new String((byte[]) objectValue);
    }
    if (objectValue instanceof String) {
      return objectValue;
    }
    return null;
  }

  @Override
  public Object convertDataValueToObjectValue(Object dataValue, Session session) {
    if (dataValue instanceof String) {
      return ((String) dataValue).getBytes();
    }
    if (dataValue instanceof byte[]) {
      return dataValue;
    }
    return null;
  }

  @Override
  public boolean isMutable() {
    return false;
  }

  @Override
  public void initialize(DatabaseMapping mapping, Session session) {

  }
}