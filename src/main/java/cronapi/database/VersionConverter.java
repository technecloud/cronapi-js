package cronapi.database;

import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.sessions.Session;

import java.nio.ByteBuffer;


public class VersionConverter implements Converter {

  @Override
  public Object convertObjectValueToDataValue(Object objectValue, Session session) {
    if (objectValue instanceof Long) {
      return objectValue;
    }
    return null;
  }

  @Override
  public Object convertDataValueToObjectValue(Object dataValue, Session session) {
    if (dataValue instanceof byte[]) {
      ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
      buffer.put((byte[]) dataValue);
      buffer.flip();
      return buffer.getLong();
    }
    if (dataValue instanceof Long) {
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