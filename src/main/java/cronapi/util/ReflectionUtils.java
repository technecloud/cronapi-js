package cronapi.util;

import java.lang.reflect.Field;

public class ReflectionUtils {
  public static void setField(Object obj, String name, Object value) {
    try {

      Field field;
      try {
        field = obj.getClass().getDeclaredField(name);
      } catch (Exception e) {
        field = obj.getClass().getSuperclass().getDeclaredField(name);
      }

      if (field != null) {
        field.setAccessible(true);
        field.set(obj, value);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static Object getField(Object obj, String name) {
    try {
      Field field;
      try {
        field = obj.getClass().getDeclaredField(name);
      } catch (Exception e) {
        field = obj.getClass().getSuperclass().getDeclaredField(name);
      }

      if (field != null) {
        field.setAccessible(true);
        return field.get(obj);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return null;
  }
}
