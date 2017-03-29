package cronapi;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;

import com.sun.scenario.Settings;

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
  
  public static boolean stringToBoolean(String str) {
    if (str == null)
      return false;
    str = str.trim();
    return str.equals("1") || str.equalsIgnoreCase("S") || str.equalsIgnoreCase("V") || str.equalsIgnoreCase("T") || str.equalsIgnoreCase("Y")
        || str.equalsIgnoreCase("true") || str.equalsIgnoreCase("verdade") || str.equalsIgnoreCase("verdadeiro") || str.equalsIgnoreCase("yes")
        || str.equalsIgnoreCase("sim") || str.equalsIgnoreCase("on");
  }


}
