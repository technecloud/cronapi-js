package cronapi.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public class AppMessages {

  private static final int SUPPORTED_LOCALES_THREESHOLD = 2;

  public static final Locale DEFAUL_LOCALE = new Locale("pt", "BR");

  private static final String BUNDLE_NAME = "i18n.Messages";

  private static final ResourceBundle DEFAULT_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, DEFAUL_LOCALE,
          new UTF8Control());

  public static final ThreadLocal<ResourceBundle> RESOURCE_BUNDLE = new ThreadLocal<>();

  private static final Map<String, Locale> SUPPORTED_LOCALES = new ConcurrentHashMap<>(
      SUPPORTED_LOCALES_THREESHOLD);

  static {
    SUPPORTED_LOCALES.put("pt", DEFAUL_LOCALE);
    SUPPORTED_LOCALES.put("en", new Locale("en", "US"));
  }

  public static String getString(String key) {
    try {
      ResourceBundle bundle = RESOURCE_BUNDLE.get();
      if (bundle == null) {
        return DEFAULT_BUNDLE.getString(key);
      } else {
        return RESOURCE_BUNDLE.get().getString(key);
      }
    } catch (MissingResourceException e) {
      return '!' + key + '!';
    }
  }

  public static String format(String pattern, Object... arguments) {
    // MessageFormat não aceita apostrofo simples diretamente.
    String fixedPattern = pattern.replace("'", "''");
    return MessageFormat.format(fixedPattern, arguments);
  }

  public static void set(Locale locale) {
    RESOURCE_BUNDLE.set(getBundle(locale));
  }

  public static void remove() {
    RESOURCE_BUNDLE.set(null);
    RESOURCE_BUNDLE.remove();
  }

  public static ResourceBundle getBundle(Locale locale) {
    Locale supportedLocale = SUPPORTED_LOCALES.getOrDefault(locale.getLanguage(), DEFAUL_LOCALE);
    return ResourceBundle.getBundle(BUNDLE_NAME, supportedLocale, new UTF8Control());
  }

  public static Locale getLocale() {
    ResourceBundle bundle = RESOURCE_BUNDLE.get();
    if (bundle == null) {
      bundle = DEFAULT_BUNDLE;
    }

    return bundle.getLocale();
  }
}
