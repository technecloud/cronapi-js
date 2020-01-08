package cronapi.database;

import com.google.gson.*;
import cronapi.AppConfig;
import cronapi.RestClient;
import cronapi.Var;
import cronapi.util.GsonUTCDateAdapter;
import cronapi.util.Operations;
import org.eclipse.persistence.descriptors.DescriptorEvent;
import org.eclipse.persistence.descriptors.DescriptorEventAdapter;
import org.eclipse.persistence.queries.UpdateObjectQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Id;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;

public class HistoryListener extends DescriptorEventAdapter {

  private static final Logger log = LoggerFactory.getLogger(HistoryListener.class);
  public static final String CURRENT_IP = getCurrentIp();
  public static GsonUTCDateAdapter UTC_DATE_ADAPTER = new GsonUTCDateAdapter();
  private static DatabaseQueryManager LOG_MANAGER;

  static {
    try {
      LOG_MANAGER = new DatabaseQueryManager("auditlogquery", false);
    } catch (Exception e) {
      //NoCommand
    }
  }

  public static DatabaseQueryManager getAuditLogManager() {
    DatabaseQueryManager logManager = null;

    try {
      if (Operations.IS_DEBUG) {
        logManager = new DatabaseQueryManager("auditlogquery", false);
      } else {
        logManager = LOG_MANAGER;
      }
    } catch (Exception e) {
      //NoCommand
    }

    return logManager;
  }

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

  private void beforeAnyOperation(DescriptorEvent event, String operation) {
    try {
      DatabaseQueryManager logManager = getAuditLogManager();

      if (logManager != null) {

        Object object = event.getObject();
        String namespace = object.getClass().getPackage().getName().replace(".entity", "");

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

        builder.registerTypeAdapter(Date.class, UTC_DATE_ADAPTER);

        Gson gson = builder.create();

        JsonElement objectJson = gson.toJsonTree(object);

        JsonArray affected = null;
        if (event.getQuery() instanceof UpdateObjectQuery) {
          affected = new JsonArray();
          for (String field : ((UpdateObjectQuery) event.getQuery()).getObjectChangeSet().getChangedAttributeNames()) {
            affected.add(field);
          }
        }

        Var auditLog = new Var(new LinkedHashMap<>());

        auditLog.set("type", object.getClass().getName());
        auditLog.set("command", operation);
        auditLog.set("category", "Entity");
        auditLog.set("date", new Date());
        auditLog.set("objectData", objectJson.toString());
        if (RestClient.getRestClient() != null) {
          auditLog.set("user", RestClient.getRestClient().getUser() != null ? RestClient.getRestClient().getUser().getUsername() : null);
          auditLog.set("host", RestClient.getRestClient().getHost());
          auditLog.set("agent", RestClient.getRestClient().getAgent());
        }
        auditLog.set("server", HistoryListener.CURRENT_IP);
        auditLog.set("affectedFields", affected != null ? affected.toString() : null);
        auditLog.set("application", AppConfig.guid());

        if (logManager.isDatabase() && logManager.getEntity().startsWith(namespace+".")) {
          logManager.insertAfterCommit(event.getSession(), auditLog);
        } else {
          logManager.insert(auditLog);
        }

      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }
}
