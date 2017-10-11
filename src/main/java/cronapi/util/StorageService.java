package cronapi.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import org.springframework.web.multipart.MultipartFile;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Classe que representa ...
 * 
 * @author Usuário de Teste
 * @version 1.0
 * @since 2017-10-04
 *
 */

public class StorageService {

	private ServletContext servletContext;
	private static String UPLOADED_FOLDER;
	private static String UPLOADED_FOLDER_GUID;
	private static String UPLOADED_FOLDER_FULLPATH;

	static {
		try {
			UPLOADED_FOLDER = "tempFilesUploads";
			UPLOADED_FOLDER_GUID = UUID.randomUUID().toString().replace("-", "");

			final File temp;
			temp = File.createTempFile(UPLOADED_FOLDER, UPLOADED_FOLDER_GUID);
			temp.delete();
			temp.mkdir();

			UPLOADED_FOLDER_FULLPATH = temp.getAbsolutePath();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static StorageServiceResult saveUploadFiles(MultipartFile[] files) {
		String savedFiles = "";
		String name = "";
		String fileExtension = "";
		String contentType = "";
		for (MultipartFile file : files) {
			if (file.isEmpty()) {
				continue;
			}

			try {
				UUID uuid = UUID.randomUUID();
				String randomUUIDString = uuid.toString().replace("-", "");

				// path = Paths.get(UPLOADED_FOLDER + File.separator + randomUUIDString + fileExtension);
				Path moveTo = Paths.get(UPLOADED_FOLDER_FULLPATH + File.separator + randomUUIDString + ".bin");
				// path.toFile().createNewFile();
				file.transferTo(moveTo.toFile());

				Path metadata = Paths.get(UPLOADED_FOLDER_FULLPATH + File.separator + randomUUIDString + ".md");
				Files.write(metadata, generateMetadata(file));

				fileExtension = "";
				if (file.getOriginalFilename().indexOf(".") > -1)
					fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().indexOf("."))
							.trim();
				name = file.getOriginalFilename().replace(fileExtension, "");
				contentType = file.getContentType();
				savedFiles = String.format("%s", Paths.get(randomUUIDString + ".bin").toString());

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		String json = String.format(
				"{\"type\": \"tempFile\", \"path\": \"%s\", \"name\": \"%s\", \"fileExtension\": \"%s\", \"contentType\": \"%s\"}",
				savedFiles, name, fileExtension, contentType);
		return new StorageServiceResult(json);
	}

	private static byte[] generateMetadata(MultipartFile file) {
		String fileExtension = "";
		if (file.getOriginalFilename().indexOf(".") > -1)
			fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().indexOf(".")).trim();
		String name = file.getOriginalFilename().replace(fileExtension, "");
		String contentType = file.getContentType();
		if (name.length() > 250)
			name = name.substring(0, 250);

		String result = String.format("{\"name\":\"%s\",\"fileExtension\":\"%s\",\"contentType\":\"%s\"}", name,
				fileExtension, contentType);
		while (result.length() < 256)
			result += " ";
		return result.getBytes();
	}

  private static JsonObject getTempFileJson(String content) {
    JsonObject tempFileJson = new JsonParser().parse(content).getAsJsonObject();
    return tempFileJson;
  }

	public static boolean isTempFileJson(String content) {
		boolean result = false;
		try {
			JsonObject tempFileJson = new JsonParser().parse(content).getAsJsonObject();
			if ("tempFile".equals(tempFileJson.get("type").getAsString()))
				result = true;
		} catch (Exception e) {
			//Abafa, não é tempfileJson, irá retornar false
		}
		return result;
	}

	public boolean deleteFile(String path) {
		try {
			return new File(path).delete();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] getFileBytesWithMetadata(String name) {
		try {
			name = getTempFileJson(name).get("path").getAsString();
			
			String pathBinary = UPLOADED_FOLDER_FULLPATH + File.separator + name;
			String pathMetadata = UPLOADED_FOLDER_FULLPATH + File.separator + name.replace(".bin", ".md");

			byte[] fileBynary = Files.readAllBytes(Paths.get(pathBinary));
			byte[] fileMetadata = Files.readAllBytes(Paths.get(pathMetadata));

			byte[] bytes = new byte[fileMetadata.length + fileBynary.length];
			System.arraycopy(fileMetadata, 0, bytes, 0, fileMetadata.length);
			System.arraycopy(fileBynary, 0, bytes, fileMetadata.length, fileBynary.length);
			return bytes;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] getFileBytesWithoutMetadata(String name) {
		try {
			name = getTempFileJson(name).get("path").getAsString();
			String pathBinary = UPLOADED_FOLDER_FULLPATH + File.separator + name;
			byte[] fileBynary = Files.readAllBytes(Paths.get(pathBinary));
			return fileBynary;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		// try {
		// 	if (path.contains("file://"))
		// 		path = path.replace("file://", "").replace(";", "");
		// 	byte[] result = Files.readAllBytes(Paths.get(path));
		// 	byte[] fileBytes = new byte[result.length - 256];
		// 	System.arraycopy(result, 256, fileBytes, 0, result.length - 256);
		// 	return fileBytes;
		// } catch (Exception e) {
		// 	throw new RuntimeException(e);
		// }
	}

	public static StorageServiceFileObject getFileObjectFromTempDirectory(String name) {
		try {
			try {
			  name = getTempFileJson(name).get("path").getAsString();
			}
			catch (Exception e) {
			  //Abafa, Vai tentar pegar diretamente do nome.
			}
			String pathBinary = UPLOADED_FOLDER_FULLPATH + File.separator + name;
			String pathMetadata = UPLOADED_FOLDER_FULLPATH + File.separator + name.replace(".bin", ".md");

			byte[] fileBynary = Files.readAllBytes(Paths.get(pathBinary));
			byte[] fileMetadata = Files.readAllBytes(Paths.get(pathMetadata));

			return generateStorageServiceFileObject(fileBynary, fileMetadata);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static StorageServiceFileObject generateStorageServiceFileObject(byte[] fileBinary, byte[] fileMetadata) {
		JsonObject metadata = new JsonParser().parse(new String(fileMetadata)).getAsJsonObject();
		return new StorageServiceFileObject(metadata.get("name").getAsString(), metadata.get("fileExtension").getAsString(),
				metadata.get("contentType").getAsString(), fileBinary);
	}

	public static StorageServiceFileObject getFileObjectFromBytes(byte[] result) {
		try {
			byte[] fileBytes = new byte[result.length - 256];
			System.arraycopy(result, 256, fileBytes, 0, result.length - 256);
			byte[] fileMetadata = new byte[256];
			System.arraycopy(result, 0, fileMetadata, 0, 256);
			return generateStorageServiceFileObject(fileBytes, fileMetadata);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Só retorna conteudo se tiver um metadata valido
	 */
	public static byte[] getFileBytesMetadata(byte[] result) {
		try {
			byte[] fileMetadata = new byte[256];
			System.arraycopy(result, 0, fileMetadata, 0, 256);
			JsonObject metadata = new JsonParser().parse(new String(fileMetadata)).getAsJsonObject();
			return fileMetadata;
		} catch (Exception e) {
			//É abafado propositalmente, pois pode ser um array sem metadata
		}
		return null;
	}

}