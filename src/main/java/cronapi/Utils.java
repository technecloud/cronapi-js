package cronapi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * Classe que representa ...
 * 
 * @author Usuário de Teste
 * @version 1.0
 * @since 2017-03-28
 *
 */

public class Utils {

	/**
	 * Construtor
	 **/
	public Utils() {
	}

	public static boolean deleteFolder(File dir) throws Exception {
		if (dir.isDirectory()) {
			Path rootPath = Paths.get(dir.getPath());
			Files.walk(rootPath, FileVisitOption.FOLLOW_LINKS).sorted(Comparator.reverseOrder()).map(Path::toFile)
					.peek(System.out::println).forEach(File::delete);
		}
		return dir.delete();
	}

	public static String MD5AsStringFromFile(File file) throws Exception {
		String filename = file.getPath();
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(Files.readAllBytes(Paths.get(filename)));
		byte[] digest = md.digest();
		String myChecksum = DatatypeConverter.printHexBinary(digest).toUpperCase();
		return myChecksum;
	}

	public static void copyFileTo(File src, File dst) throws Exception {
		if (src == null || dst == null) {
			return;
		}
		Files.copy(Paths.get(src.getPath()), Paths.get(dst.getPath()), StandardCopyOption.REPLACE_EXISTING);
	}

	public static StringBuilder getFileContent(FileInputStream fstream) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream) fstream));
		StringBuilder out = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			out.append(line);
			out.append(System.getProperty("line.separator"));
		}
		reader.close();
		return out;
	}

	public static String getFileContent(String file) throws Exception {
		return FileUtils.readFileToString(new File(file));
	}

	public static boolean stringToBoolean(final String str) {
		if (str == null)
			return false;
		return Boolean.valueOf(str.trim());
	}

	public static byte[] getFromBase64(String base64) {
		byte[] bytes = null;
		if (base64 != null && !base64.equals("")) {
			bytes = Base64.getDecoder().decode(base64);
		}
		return bytes;
	}

	public static String stringToJs(String string) {
		return StringEscapeUtils.escapeJavaScript(string);
	}

	public static int getFromCalendar(Date date, int field) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c.get(field);
	}

	public static Method findMethod(Object obj, String method) {
		Method[] methods = obj.getClass().getMethods();
		for (Method m : methods) {
			if (m.getName().equalsIgnoreCase(method)) {
				return m;
			}
		}
		return null;
	}

}
