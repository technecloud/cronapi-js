package cronapi.database;

import cronapi.AppConfig;
import cronapi.RestClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;
import org.eclipse.persistence.internal.jpa.deployment.PersistenceUnitProcessor;
import org.eclipse.persistence.internal.jpa.deployment.SEPersistenceUnitInfo;
import org.eclipse.persistence.jpa.Archive;

public class TransactionManager {

  public static final String AUTO = "AUTO";
  private static ThreadLocal<Map<String, EntityManager>> CACHE_NAMESPACE = new ThreadLocal<>();

  public static void addNamespace(String namespace, EntityManager entityManager) {
    Map<String, EntityManager> map = CACHE_NAMESPACE.get();
    if (map == null) {
      map = new HashMap<>();
      CACHE_NAMESPACE.set(map);
    }

    map.put(namespace, entityManager);
  }


  public static EntityManager getEntityManager(Class domainClass) {
    return getEntityManager(domainClass, true);
  }

  public static EntityManager getEntityManager(Class domainClass, boolean cache) {
    String namespace = domainClass.getPackage().getName().replace(".entity", "");
    return getEntityManager(namespace, cache);
  }

  public static EntityManager getEntityManager(String namespace, boolean cache) {

    Map<String, EntityManager> mapNamespace = CACHE_NAMESPACE.get();

    if (mapNamespace == null) {
      mapNamespace = new HashMap<>();
      CACHE_NAMESPACE.set(mapNamespace);
    }


    if (mapNamespace != null && cache) {
      EntityManager emNamespace = mapNamespace.get(namespace);
      if (emNamespace != null) {
        return emNamespace;
      }
    }

    EntityManagerFactory factory = findEntityManagerFactory(namespace);

    EntityManager em = factory.createEntityManager();
    em.setFlushMode(AppConfig.flushMode());
    if (cache) {
      CACHE_NAMESPACE.get().put(namespace, em);
    }

    TenantService tenantService = RestClient.getRestClient().getTenantService();

    if (tenantService != null && tenantService.getContextIds() != null) {
      Set<String> keySet = tenantService.getContextIds().keySet();
      for (String key : keySet) {
        em.setProperty(key, tenantService.getId(key));
      }
    }
    if (!cache) {
      em.setProperty("eclipselink.query-results-cache", "false");
      em.setProperty("eclipselink.cache.shared.default", "false");
    }
    return em;
  }

  public static EntityManagerFactory findEntityManagerFactory(Class domainClass) {
    String namespace = domainClass.getPackage().getName().replace(".entity", "");
    return findEntityManagerFactory(namespace);
  }

  public static EntityManagerFactory findEntityManagerFactory(String namespace) {

    Set<Archive> archives = PersistenceUnitProcessor.findPersistenceArchives();

    for (Archive archive : archives) {

      List<SEPersistenceUnitInfo> persistenceUnitInfos = PersistenceUnitProcessor.getPersistenceUnits(archive, Thread.currentThread().getContextClassLoader());

      for (SEPersistenceUnitInfo pui : persistenceUnitInfos) {

        if (pui.getPersistenceUnitName().equals(namespace)) {
          EntityManagerFactory factory = Persistence.createEntityManagerFactory(namespace);
          return factory;
        }
      }
    }

    return null;
  }

  public static void commit(Class domainClass) {
    EntityManager em = getEntityManager(domainClass);
    if (em != null) {
      if (em.getTransaction().isActive()) {
        em.getTransaction().commit();
      }
    }
  }

  public static void flush(Class domainClass) {
    EntityManager em = getEntityManager(domainClass);
    if (em != null) {
      if (em.getTransaction().isActive()) {
        em.flush();
      }
    }
  }

  public static void begin(Class domainClass) {
    EntityManager em = getEntityManager(domainClass);
    if (em != null) {
      if (!em.getTransaction().isActive()) {
        em.getTransaction().begin();
      }
    }
  }

  public static void rollback(Class domainClass) {
    EntityManager em = getEntityManager(domainClass);
    if (em != null) {
      if (em.getTransaction().isActive()) {
        em.getTransaction().rollback();
      }
    }
  }

  public static void close(Class domainClass) {
    EntityManager em = getEntityManager(domainClass);
    if (em != null) {
      em.close();
    }
  }

  public static void commit() {
    Map<String, EntityManager> mapNamespace = CACHE_NAMESPACE.get();
    if (mapNamespace != null) {
      for (EntityManager em : mapNamespace.values()) {
        if (em.getTransaction().isActive()) {
          em.getTransaction().commit();
        }
      }
    }
  }

  public static void flush() {
    Map<String, EntityManager> mapNamespace = CACHE_NAMESPACE.get();
    if (mapNamespace != null) {
      for (EntityManager em : mapNamespace.values()) {
        em.flush();
      }
    }
  }

  public static void rollback() {
    Map<String, EntityManager> mapNamespace = CACHE_NAMESPACE.get();
    if (mapNamespace != null) {
      for (EntityManager em : mapNamespace.values()) {
        if (em.getTransaction().isActive()) {
          em.getTransaction().rollback();
        }
      }
    }
  }

  public static void close() {
    Map<String, EntityManager> mapNamespace = CACHE_NAMESPACE.get();
    if (mapNamespace != null) {
      for (EntityManager em : mapNamespace.values()) {
        if (em.isOpen()) {
          em.close();
        }
      }
    }
  }

  public static void clear() {
    Map<String, EntityManager> map = CACHE_NAMESPACE.get();
    if (map != null) {
      for (EntityManager em : map.values()) {
        try {
          em.clear();
        } catch (Exception e) {
          //Abafa
        }
      }
      map.clear();
    }

    CACHE_NAMESPACE.set(null);
    CACHE_NAMESPACE.remove();
  }

  public static void setFlushMode(String namespace, String flushMode) {
    EntityManager em = getEntityManager(namespace, true);
    if (AUTO.equalsIgnoreCase(flushMode))
      em.setFlushMode(FlushModeType.AUTO);
    else
      em.setFlushMode(FlushModeType.COMMIT);
  }
}