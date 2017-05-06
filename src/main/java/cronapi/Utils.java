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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.xml.bind.DatatypeConverter;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import cronapi.i18n.Messages;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Classe que representa ...
 * 
 * @author Usuário de Teste
 * @version 1.0
 * @since 2017-03-28
 *
 */

public class Utils {

	private static final Map<String, DateFormat[]> DATE_FORMATS = new HashMap<>();

	private static final Map<String, DateFormat> DATETIME_FORMAT = new HashMap<>();

	private static final Map<String, DateFormat> PARSE_DATETIME_FORMAT = new HashMap<>();

	static {
		DATE_FORMATS.put("pt", getGenericParseDateFormat(new Locale("pt", "BR")));
		DATE_FORMATS.put("en", getGenericParseDateFormat(new Locale("en", "US")));

		PARSE_DATETIME_FORMAT.put("pt", new SimpleDateFormat(Messages.getBundle(new Locale("pt", "BR")).getString("ParseDateFormat")));
		PARSE_DATETIME_FORMAT.put("en", new SimpleDateFormat(Messages.getBundle(new Locale("en", "US")).getString("ParseDateFormat")));

		DATETIME_FORMAT.put("pt", new SimpleDateFormat(Messages.getBundle(new Locale("pt", "BR")).getString("DateTimeFormat")));
		DATETIME_FORMAT.put("en", new SimpleDateFormat(Messages.getBundle(new Locale("en", "US")).getString("DateTimeFormat")));
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

	public static Calendar toGenericCalendar(String value) {
		Date date = null;
		try {
			if (NumberUtils.isNumber(value)) {
				Double d = Double.valueOf(value);
				date = new Date(d.longValue());
			}
		} catch (Exception e) {
			//
		}

		if (date == null) {
			DateFormat[] formats = DATE_FORMATS.get(Messages.getLocale().getLanguage());
			if (formats == null) {
				formats = DATE_FORMATS.get("pt");
			}
			for (DateFormat format : formats) {
				try {
					date = format.parse(value);
					break;
				} catch (Exception e2) {
					//Abafa
				}
			}
		}

		if (date != null) {
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			return c;
		}

		return null;
	}

	public static Calendar toCalendar(String value, String mask) {
		if (value == null) {
			return null;
		}

		try {
			if (mask != null && !mask.isEmpty()) {
				SimpleDateFormat format = new SimpleDateFormat(mask);
				Date date = format.parse(value);
				Calendar c = Calendar.getInstance();
				c.setTime(date);
				return c;
			}
		} catch (Exception e) {
			//
		}

		return toGenericCalendar(value);
	}

	public static final DateFormat getParseDateFormat () {
		DateFormat format = PARSE_DATETIME_FORMAT.get(Messages.getLocale().getLanguage());
		if (format == null) {
			format = PARSE_DATETIME_FORMAT.get("pt");
		}

		return format;
	}

	public static final DateFormat getDateFormat () {
		DateFormat format = DATETIME_FORMAT.get(Messages.getLocale().getLanguage());
		if (format == null) {
			format = DATETIME_FORMAT.get("pt");
		}

		return format;
	}

	private static DateFormat[] getGenericParseDateFormat(Locale locale) {
		String datePattern = Messages.getBundle(locale).getString("ParseDateFormat");

		final String[] formats = { (datePattern + " H:m:s.SSS"), (datePattern + " H:m:s"), (datePattern + " H:m"), "yyyy-M-d H:m:s.SSS", "yyyy-M-d H:m:s",
				"yyyy-M-d H:m", datePattern, "yyyy-M-d", "H:m:s", "H:m" };

		DateFormat[] dateFormats = new DateFormat[formats.length + 1];
		dateFormats[0] = new ISO8601DateFormat();

		for (int i=0;i<formats.length;i++) {
			dateFormats[i+1] = new SimpleDateFormat(formats[i]);
		}

		return dateFormats;
	}

}
