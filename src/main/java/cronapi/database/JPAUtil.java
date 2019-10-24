package cronapi.database;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import java.util.LinkedHashSet;
import java.util.Set;

public class JPAUtil {
  public static Set getAjustedAttributes(EntityType type) {
    Set<SingularAttribute> attributes = type.getAttributes();
    Set<SingularAttribute> attrs = new LinkedHashSet<SingularAttribute>();

    for (int i = 0; i < type.getJavaType().getDeclaredFields().length; i++) {
      for (SingularAttribute attr : attributes) {
        if (attr.getName().equalsIgnoreCase(type.getJavaType().getDeclaredFields()[i].getName())) {
          attrs.add(attr);
          break;
        }
      }
    }

    return attrs;
  }
}

