package cronapi;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classe que representa ...
 * 
 * @author Usuário de Teste
 * @version 1.0
 * @since 2017-03-28
 *
 */

public class Functions {

	/**
	 * Construtor
	 **/
	public Functions() {
	}

	public static boolean deleteFolder(File dir) {
		if (dir.isDirectory()) {
			File[] children = dir.listFiles();
			for (int i = 0; i < children.length; i++) {
				File child = children[i];
				if (child.isDirectory())
					deleteFolder(child);
				else
					child.delete();
			}
		}
		return dir.delete();
	}

	public static int[] convertBytes(byte[] b) {
		int[] r = new int[b.length];
		for (int i = 0; i < b.length; i++) {
			r[i] = 0x00FF & b[i];
		}
		return r;
	}

	public static String fix2Zeros(String h) {
		if (h.length() == 0) {
			return "00";
		} else if (h.length() == 1) {
			return "0" + h;
		} else {
			return h;
		}
	}

	public static String fix2Zeros(int s) {
		if (s < 10) {
			return "0" + s;
		} else {
			return String.valueOf(s);
		}
	}

	public static String MD5AsStringFromFile(File file) throws Exception {
		DataInputStream in = null;
		FileInputStream fstream = null;

		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");

			fstream = new FileInputStream(file);
			in = new DataInputStream(fstream);
			byte[] bin = new byte[254];
			while (in.available() != 0) {
				int bytes = in.read(bin);
				md5.update(bin, 0, bytes);
			}

			int idx;

			byte[] b = md5.digest();
			int[] hash = convertBytes(b);
			String result = "";
			for (idx = 0; idx < hash.length; idx++) {
				result += fix2Zeros(Integer.toHexString(hash[idx]));
			}
			return result;
		} finally {
			if (in != null)
				in.close();
			if (fstream != null)
				fstream.close();
		}
	}

	public static void copyFileTo(File src, File dst) throws Exception {
		if (src == null || dst == null) {
			return;
		}

		if (!dst.exists()) {
			dst.getParentFile().mkdirs();
		}

		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(src);
			out = new FileOutputStream(dst);

			dst.getParentFile().mkdirs();

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		} finally {
			if (in != null) {
				in.close();
			}

			if (out != null) {
				out.close();
			}

			dst.setLastModified(src.lastModified());
		}
	}

	public static StringBuffer getFileContent(FileInputStream fstream) {
		StringBuffer r = new StringBuffer();
		DataInputStream in = null;
		try {
			in = new DataInputStream(fstream);
			byte[] b = new byte[254];
			while (in.available() != 0) {
				int bytes = in.read(b);
				r.append(new String(b, 0, bytes));
			}
		} catch (Exception e) {
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		return r;
	}
	
	public static StringBuffer getFileContent(String file) {
    StringBuffer r = new StringBuffer();
    DataInputStream in = null;
    FileInputStream fstream = null;
    try {
      fstream = new FileInputStream(file);
      in = new DataInputStream(fstream);
      byte[] b = new byte[254];
      while (in.available() != 0) {
        int bytes = in.read(b);
        r.append(new String(b, 0, bytes, "UTF-8"));
      }
    } catch (Exception e) {
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException ex) {
          //
        }
      }

      if (fstream != null) {
        try {
          fstream.close();
        } catch (IOException ex) {
          //
        }
      }
    }
    return r;
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
    return stringToJs(string, false);
  }

  public static String stringToJs(String string, boolean onlyOneLine) {
    return stringToJs(string, onlyOneLine, false);
  }

  public static String stringToJs(String string, boolean onlyOneLine, boolean preventCrossSiteScripting) {
    if (string != null) {
      StringBuffer sb = new StringBuffer(string.length());
      int len = string.length();
      char c;
      for (int i = 0; i < len; i++) {
        c = string.charAt(i);
        if (c == '\\') {
          sb.append("\\\\");
        } else if (c == '\'') {
          sb.append("\\'");
        } else if (c == '"') {
          sb.append("\\\"");
        } else if (c == '\n') {
          if (!onlyOneLine)
            sb.append("\\n");
          else
            sb.append(" ");
        } else if (c == '\r') {
        } else {
          sb.append(c);
        }
      }
      if (preventCrossSiteScripting) {
        return splitTags(sb.toString());
      }
      return sb.toString().replaceAll("<script", "<scr\'+\'ipt").replaceAll("</script", "</scr\'+\'ipt");
    } else {
      return "";
    }
  }
  
  public static String splitTags(String value) {
    StringBuilder result = new StringBuilder();
    Pattern pattern = Pattern.compile("<([^<>\\s\\/]+)[^<>]*>|<\\/([^<>\\s\\/]+)");
    Matcher matcher = pattern.matcher(value);
    int lastIndex = 0;
    while (matcher.find()) {
      String tagName1 = matcher.group(1);
      String tagName;
      int start;
      int end;
      if (tagName1 != null) {
        start = matcher.start(1);
        end = matcher.end(1);
        tagName = tagName1;
      } else {
        start = matcher.start(2);
        end = matcher.end(2);
        tagName = matcher.group(2);
      }
      result.append(value.substring(lastIndex, start));
      result.append(splitValue(tagName, "\'+\'"));
      lastIndex = end;
    }
    result.append(value.substring(lastIndex));
    return result.toString();
  }
  
  public static String splitValue(String value, String separator) {
    if (value == null) {
      return null;
    }
    if (value.length() <= 1) {
      return value;
    }
    int splitIndex = (int) Math.floor(((double) value.length() / 2));
    return value.substring(0, splitIndex) + separator + value.substring(splitIndex, value.length());
  }


}
