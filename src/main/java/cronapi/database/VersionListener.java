package cronapi.database;

import cronapi.i18n.Messages;
import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.descriptors.DescriptorEvent;
import org.eclipse.persistence.descriptors.DescriptorEventAdapter;

import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.Query;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class VersionListener extends DescriptorEventAdapter {

  private boolean versionChecked = false;
  private List<Field> versionFields;
  private List<Field> pkFields;

  @Override
  public void aboutToUpdate(DescriptorEvent event) {
    try {
      if (!versionChecked) {
        for (Field field : event.getObject().getClass().getDeclaredFields()) {
          Convert convert = field.getAnnotation(Convert.class);
          if (convert != null && convert.value().equals("version")) {
            if (versionFields == null) {
              versionFields = new LinkedList<>();
            }
            versionFields.add(field);
            field.setAccessible(true);
          }

          Id id = field.getAnnotation(Id.class);
          if (id != null) {
            if (pkFields == null) {
              pkFields = new LinkedList<>();
            }
            pkFields.add(field);
            field.setAccessible(true);
          }
        }
        versionChecked = true;
      }
      if (versionChecked && versionFields != null && pkFields != null) {
        EntityManager em = TransactionManager.getEntityManager(event.getObject().getClass(), true);
        for (Field field : versionFields) {
          Object value = field.get(event.getObject());

          if (value != null) {
            String jpql = "select e." + field.getName() + " from " + event.getObject().getClass().getName() + " e where ";

            int i = 0;
            for (Field pk : pkFields) {
              if (i > 0) {
                jpql += " AND ";
              }

              jpql += "e." + pk.getName() + "  = :" + pk.getName();
              i++;
            }

            if (i > 0) {
              jpql += " AND ";
            }

            jpql += "e." + field.getName() + "  = :" + field.getName();
            i++;

            Query query = em.createQuery(jpql);

            for (Field pk : pkFields) {
              Field refField = event.getObject().getClass().getDeclaredField(pk.getName());
              refField.setAccessible(true);
              query.setParameter(pk.getName(), refField.get(event.getObject()));
            }
            query.setParameter(field.getName(), field.get(event.getObject()));

            query.setHint("javax.persistence.cache.storeMode", "REFRESH");

            List result = query.getResultList();

            Object current = result.size() > 0 ? result.get(0) : null;

            if (current == null) {
              throw new RuntimeException(Messages.getString("optimisticLockingError"));
            }
            Object currentValue = result.get(0);
            Object newValue = field.get(event.getObject());
            if (!Objects.equals(currentValue, newValue)) {
              throw new RuntimeException(Messages.getString("optimisticLockingError"));
            }
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}