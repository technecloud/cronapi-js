package cronapi.clazz;

import cronapi.CronapiConfigurator;
import cronapi.Var;
import cronapi.util.Operations;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Hashtable;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class CronapiClassLoader extends ClassLoader {
  
  private Hashtable classes = new Hashtable();

  private static String CLASS_FOLDER;

  static {
    CLASS_FOLDER = classFolder();
  }

  public CronapiClassLoader() {
    super(CronapiClassLoader.class.getClassLoader()); // calls the parent class loader's constructor
  }

  @Override
  public Class loadClass(String className) throws ClassNotFoundException {
    return findClass(className);
  }

  @Override
  public Class findClass(String className) throws ClassNotFoundException {
    Class clazz = (Class) classes.get(className);

    if (clazz != null)
      return clazz;

    byte classData[];
    try {
      String path = className.replace('.', File.separatorChar);
      File classFile = new File(classFolder(Class.forName(className), false),  path + ".class");

      if(classFile.exists()) {

        if (className.startsWith("cronapi.")) {
          return Class.forName(className);
        }

        try (FileInputStream fi = new FileInputStream(classFile)) {
          classData = IOUtils.toByteArray(fi);
          clazz = defineClass(className, classData, 0, classData.length);
          classes.put(className, clazz);
          return clazz;
        }
      }
      else {
        return Class.forName(className);
      }
    }
    catch(Exception e) {
      throw new ClassNotFoundException(e.getMessage(), e);
    }
  }

  public static String classFolder() {
    return classFolder(ClassLoader.class, false);
  }

  public static String classFolder(Class<?> clazz, Boolean usePackagePath) {
    String classFolder = "";

    try {
      Class<CronapiClassLoader> thisClass = CronapiClassLoader.class;
      if(clazz.getProtectionDomain() != null && clazz.getProtectionDomain().getCodeSource() != null) {
        classFolder = new File(clazz.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();

        String windowsSlash = (Operations.IS_WINDOWS ? "/?" : "");
        classFolder = classFolder.replaceAll("file:" + windowsSlash + "|vfs:" + windowsSlash +
                "|[/\\\\]wfr[/\\\\]util[/\\\\]" + thisClass.getSimpleName() + ".class", "");

        String path = clazz.getCanonicalName().replace(".", File.separator) + ".class";
        if (classFolder.endsWith(path)) {
          classFolder = classFolder.substring(0, classFolder.length() - path.length());
        }
      }
    }
    catch(Exception e) {
      classFolder = "";
    }
    
    if(usePackagePath) {
      return fix(classFolder + File.separatorChar + clazz.getPackage().getName().replace('.', File.separatorChar));
    }
    else {
      return fix(classFolder);
    }
  }
  
  private static String fix(String path) {
    return path.replace('\\', File.separatorChar).replace('/', File.separatorChar);
  }
  
}
