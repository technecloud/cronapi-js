package cronapi.database;

import com.google.gson.*;
import cronapi.RestClient;
import cronapi.Utils;
import cronapi.util.GsonUTCDateAdapter;
import cronapi.util.ReflectionUtils;
import org.eclipse.persistence.descriptors.DescriptorEvent;
import org.eclipse.persistence.descriptors.DescriptorEventAdapter;
import org.eclipse.persistence.queries.UpdateObjectQuery;

import javax.persistence.Id;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Date;
import java.util.Enumeration;

public class HistoryListener extends DescriptorEventAdapter {

  private static final String CURRENT_IP = getCurrentIp();
  private GsonUTCDateAdapter UTCDateAdaper = new GsonUTCDateAdapter();

  @Override
  public void postUpdate(DescriptorEvent event) {
    beforeAnyOperation(event, "UPDATE");
  }

  @Override
  public void postInsert(DescriptorEvent event) {
    beforeAnyOperation(event, "INSERT");
  }

  @Override
  public void postDelete(DescriptorEvent event) {
    beforeAnyOperation(event, "DELETE");
  }

  private static String getCurrentIp() {

    try {
      Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
      while (networkInterfaces.hasMoreElements()) {
        NetworkInterface ni = networkInterfaces.nextElement();
        Enumeration<InetAddress> nias = ni.getInetAddresses();
        while (nias.hasMoreElements()) {
          InetAddress ia = nias.nextElement();
          if (!ia.isLinkLocalAddress() && !ia.isLoopbackAddress() && ia instanceof Inet4Address) {
            return ia.getHostAddress();
          }
        }
      }
    } catch (SocketException e2) {
      // Abafa
    }

    return null;
  }

  private JsonElement toElement(Gson gson, Object value) {
    if (value instanceof Date) {
      return new JsonPrimitive(Utils.getISODateFormat().format((Date) value));
    } else {
      JsonElement element = gson.toJsonTree(value);
      return element;
    }
  }

  private void beforeAnyOperation(DescriptorEvent event, String operation) {

    Class auditClazz = null;
    Object object = event.getObject();

    try {
      auditClazz = Class.forName(object.getClass().getPackage().getName() + ".AuditLog");

      GsonBuilder builder = new GsonBuilder().addSerializationExclusionStrategy(new ExclusionStrategy() {
        @Override
        public boolean shouldSkipField(FieldAttributes fieldAttributes) {
          if (fieldAttributes.getDeclaringClass() == object.getClass() || fieldAttributes.getAnnotation(Id.class) != null) {
            return false;
          }
          return true;
        }

        @Override
        public boolean shouldSkipClass(Class<?> aClass) {
          return false;
        }
      });

      builder.registerTypeAdapter(Date.class, UTCDateAdaper);

      Gson gson = builder.create();

      JsonElement objectJson = gson.toJsonTree(object);

      JsonArray affected = null;
      if (event.getQuery() instanceof UpdateObjectQuery) {
        affected = new JsonArray();
        for (String field : ((UpdateObjectQuery) event.getQuery()).getObjectChangeSet().getChangedAttributeNames()) {
          affected.add(field);
        }
      }

      Object auditLog = auditClazz.newInstance();

      ReflectionUtils.setField(auditLog, "type", object.getClass().getName());
      ReflectionUtils.setField(auditLog, "command", operation);
      ReflectionUtils.setField(auditLog, "date", new Date());
      ReflectionUtils.setField(auditLog, "objectData", objectJson.toString());
      if (RestClient.getRestClient() != null) {
        ReflectionUtils.setField(auditLog, "user", RestClient.getRestClient().getUser() != null ? RestClient.getRestClient().getUser().getUsername() : null);
        ReflectionUtils.setField(auditLog, "host", RestClient.getRestClient().getHost());
        ReflectionUtils.setField(auditLog, "agent", RestClient.getRestClient().getAgent());
      }
      ReflectionUtils.setField(auditLog, "server", CURRENT_IP);
      ReflectionUtils.setField(auditLog, "affectedFields", affected != null ? affected.toString() : null);

      event.getSession().insertObject(auditLog);

      System.out.println(objectJson.toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] x) throws Exception {
    Gson gson = new Gson();
    JsonElement element = gson.toJsonTree(null);

    System.out.println(element.toString());
  }
}
