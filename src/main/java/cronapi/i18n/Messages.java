package cronapi.i18n;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages { 
  
  private static final String BUNDLE_NAME = "cronapi.i18n.Messages";
  
  private static final ResourceBundle DEFAULT_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, new Locale("pt", "BR"));

  public static final ThreadLocal<ResourceBundle> RESOURCE_BUNDLE = new ThreadLocal<>();

  public static String getString(String key) {
    try {
      ResourceBundle bundle = RESOURCE_BUNDLE.get();
      if (bundle == null)
        return DEFAULT_BUNDLE.getString(key);
      else
        return RESOURCE_BUNDLE.get().getString(key);
    }
    catch(MissingResourceException e) {
      return '!' + key + '!';
    }
  }
  
  public static String format(String pattern, Object ... arguments) {
    // MessageFormat não aceita apostrofo simples diretamente.
    String fixedPattern = pattern.replace("'", "''");
    return MessageFormat.format(fixedPattern, arguments);
  }
  
  public static void set(Locale locale) {
    RESOURCE_BUNDLE.set(ResourceBundle.getBundle(BUNDLE_NAME, locale));
  }

  public static ResourceBundle getBundle(Locale locale) {
    return ResourceBundle.getBundle(BUNDLE_NAME, locale);
  }

  public static Locale getLocale() {
      ResourceBundle bundle = RESOURCE_BUNDLE.get();
      if (bundle == null)
        bundle = DEFAULT_BUNDLE;

      return bundle.getLocale();
  }
}
