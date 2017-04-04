package cronapi.io;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cronapi.CronapiMetaData;
import cronapi.Functions;
import cronapi.Var;

/**
 * Classe que representa ...
 * 
 * @author Usuário de Teste
 * @version 1.0
 * @since 2017-03-28
 *
 */

@CronapiMetaData(category="IO", categorySynonymous={"Arquivo", "File"})
public class Operations {

	/**
	 * Criar nova pasta 
	 */
	@CronapiMetaData(type="function", name="{{createFolder}}", nameSynonymous={"createFolder"}, description="{{functionToCreateNewFolder}}", params={"{{pathMustBeCreatedForFolder}}"})
	protected static final Var folderCreate(Var path) throws Exception {
		boolean created = true;
		File dir = new File(path.getObjectAsString().trim());
		if (!dir.exists()) {
			created = dir.mkdirs();
		}
		return new Var(created);
	}

	/**
	 * MD5 do Arquivo
	 */
	@CronapiMetaData(type="function", name="{{MD5OfFile}}", nameSynonymous={"fileMD5"},description="{{functionToReturnMD5OfFile}}", params={"{{pathOfFile}}"})
	protected static final Var fileMD5(Var path) throws Exception {
		return new Var(Functions.MD5AsStringFromFile(new File(path.getObjectAsString().trim())));
	}

	/**
	 * Remover Pasta de Arquivos
	 */
	@CronapiMetaData(type="function", name="{{removeFolderFiles}}", nameSynonymous={"removeFolder", "deleteFolder"}, description="{{functionToRemoveFolderFiles}}", params={"{{pathOfFolder}}"})
	protected static final Var fileRemoveFolderAndChildren(Var path) throws Exception {
		File dir = new File(path.getObjectAsString().trim());
		return new Var(Functions.deleteFolder(dir));
	}

	/**
	 * Obter MD5 do Arquivo
	 */
	@CronapiMetaData(type="function", name="{{getMD5OfFile}}", nameSynonymous={"getFileMD5"}, description="{{functionToGetMD5OfFile}}", params={"{{pathOfFile}}"})
	protected static final Var getFileMD5(Var fileToGetMD5) throws Exception {
		java.io.File file = new java.io.File(fileToGetMD5.getObjectAsString().trim());
		DataInputStream in = null;
		FileInputStream fstream = null;

		try {
			java.security.MessageDigest md5 = java.security.MessageDigest.getInstance("MD5");

			fstream = new java.io.FileInputStream(file);
			in = new java.io.DataInputStream(fstream);
			byte[] bin = new byte[254];
			while (in.available() != 0) {
				int bytes = in.read(bin);
				md5.update(bin, 0, bytes);
			}

			int idx;

			byte[] b = md5.digest();
			int[] hash = Functions.convertBytes(b);
			String result = "";
			for (idx = 0; idx < hash.length; idx++) {
				result += Functions.fix2Zeros(Integer.toHexString(hash[idx]));
			}
			return new Var(result);
		} finally {
			if (in != null)
				in.close();
			if (fstream != null)
				fstream.close();
		}
	}

	/**
	 * Pode Ler?
	 */
	@CronapiMetaData(type="function", name="{{canReadyFile}}", nameSynonymous={"fileCanRead"}, description="{{functionToCheckIfCanReadFile}}", params={"{{pathOfFile}}"})
	protected final Var fileCanRead(Var path) throws Exception {
		File file = new File(path.getObjectAsString().trim());
		return new Var(file.canRead());
	}

	/**
	 * Pode Escrever?
	 */
	@CronapiMetaData(type="function", name="{{canWriteFile}}", nameSynonymous={"fileCanWrite"}, description="{{functionToCheckIfCanWriteFile}}", params={"{{pathOfFile}}"})
	protected final Var fileCanWrite(Var path) throws Exception {
		File file = new File(path.getObjectAsString().trim());
		return new Var(file.canWrite());
	}

	/**
	 * Criar Novo Arquivo
	 */
	@CronapiMetaData(type="function", name="{{createNewFile}}", nameSynonymous={"fileCreate"}, description="{{functionToCreateFile}}", params={"{{pathOfFile}}"})
	protected static final void fileCreate(Var path) throws Exception {
		File file = new File(path.getObjectAsString().trim());
		if (!file.exists()) {
			OutputStream out = new FileOutputStream(file);
			out.close();
		}
	}

	/**
	 * Remover Arquivo
	 */
	@CronapiMetaData(type="function", name="{{removeFile}}", nameSynonymous={"fileRemove"}, description="{{functionToRemoveFile}}", params={"{{pathOfFile}}"})
	protected static final Var fileRemove(Var path) throws Exception {
		File file = new File(path.getObjectAsString().trim());
		return new Var(file.delete());
	}

	/**
	 * Existe o Arquivo?
	 */
	@CronapiMetaData(type="function", name="{{fileExists}}", nameSynonymous={"fileExists"}, description="{{functionToCheckIfExistFile}}", params={"{{pathOfFile}}"})
	protected static final Var fileExists(Var path) throws Exception {
		File file = new File(path.getObjectAsString().trim());
		return new Var(file.exists());
	}


	/**
	 * Copiar Arquivo
	 */
	@CronapiMetaData(type="function", name="{{copyFile}}", nameSynonymous={"fileCopy"}, description="{{functionToCopyFile}}", params={"{{sourcePath}}", "{{destinationPath}}"})
	protected static final void fileCopy(Var pathFrom, Var pathTo) throws Exception {
		File from = new File(pathFrom.getObjectAsString().trim());
		File to = new File(pathTo.getObjectAsString().trim());
		Functions.copyFileTo(from, to);
	}

	/**
	 * Obter Pai do Arquivo
	 */
	@CronapiMetaData(type="function", name="{{getParentOfFile}}", nameSynonymous={"fileGetParent"}, description="{{functionToGetParentOfFile}}", params={"{{pathOfFile}}"})
	protected static final Var fileGetParent(Var path) throws Exception {
		File file = new File(path.getObjectAsString().trim());
		if (file.exists()) {
			return new Var(file.getParent());
		} else {
			return new Var("");
		}
	}

	/**
	 * Renomear Arquivo
	 */
	@CronapiMetaData(type="function", name="{{renameFile}}", nameSynonymous={"fileRename"}, description="{{functionToRenameFile}}", params={"{{pathOfFile}}", "{{newNameOfFile}}"})
	protected static final Var fileRename(Var path, Var name) throws Exception {
		File from = new File(path.getObjectAsString().trim());
    File to = new File(from.getParentFile(), name.getObjectAsString().trim());
		return new Var(from.renameTo(to));
	}

	/**
	 * Mover Arquivo
	 */
	@CronapiMetaData(type="function", name="{{moveFile}}", nameSynonymous={"fileMove"}, description="{{functionToMoveFile}}", params={"{{pathOfSourceFile}}", "{{pathOfDestinationFile}}"})
	protected static final Var fileMove(Var pathFrom, Var pathTo) throws Exception {
		File from = new File(pathFrom.getObjectAsString().trim());
		File to = new File(pathTo.getObjectAsString().trim());
		return new Var(from.renameTo(to));
	}

	/**
	 * Forçar criação do diretorio para o arquivo
	 */
	@CronapiMetaData(type="function", name="{{forceFolderCreateToFile}}", nameSynonymous={"forceDirectories", "forceFolder", "forceCreateDirectories", "forceCreateFolder"}, description="{{functionToForceFolderCreateToFile}}", params={"{{pathOfFile}}"})
	protected static final Var forceDirectories(Var path) throws Exception {
		return new Var(new File(path.getObjectAsString()).getParentFile().mkdirs());
	}

	/**
	 * Abrir arquivo para escrita
	 */
	@CronapiMetaData(type="function", name="{{openFileToWrite}}", nameSynonymous={"fileOpenToWrite"}, description="{{functionToOpenFileToWrite}}", params={"{{pathOfFile}}", "{{addContent}}"})
	protected static final Var fileOpenToWrite(Var url, Var append) throws Exception {
		FileOutputStream out = new FileOutputStream(new File(url.getObjectAsString()), append.getObjectAsBoolean());
		return new Var(out);
	}

	/**
	 * Abrir arquivo para leitura
	 */
	@CronapiMetaData(type="function", name="{{openFileToRead}}", nameSynonymous={"fileOpenToRead"}, description="{{functionToOpenFileToRead}}", params={"{{pathOfFile}}"})
	protected static final Var fileOpenToRead(Var url) throws Exception {
		FileInputStream in = new FileInputStream(new File(url.getObjectAsString()));
		return new Var(in);
	}

	/**
	 * Adicionar conteúdo a arquivo
	 */
	@CronapiMetaData(type="function", name="{{addContentToFile}}", nameSynonymous={"fileAppend"}, description="{{functionToAddContentToFile}}", params={"{{streamOfFileToWrite}}", "{{contentOfFile}}"})
	protected static final void fileAppend(Var outPut, Var content) throws Exception {
		FileOutputStream out = (FileOutputStream)outPut.getObject();
		if(content.getObject() instanceof byte[])
      out.write((byte[])content.getObject());
    else
      out.write(content.getObjectAsString().getBytes());
	}

	/**
	 * Ler conteúdo do arquivo
	 */
	@CronapiMetaData(type="function", name="{{readContentOfFile}}", nameSynonymous={"fileRead"}, description="{{functionToReadContentOfFile}}", params={"{{streamOfFileToRead}}", "{{size}}"})
	protected static final Var fileRead(Var input, Var size) throws Exception {
		byte[] b = new byte[size.getObjectAsInt()];
		FileInputStream in = (FileInputStream)input.getObject();
		if (in.available() != 0) {
			int bytes = in.read(b);
			return new Var(new String(b, 0, bytes));
		}
		return new Var(null);
	}

	/**
	 * Ler todo contéudo do arquivos
	 */
	@CronapiMetaData(type="function", name="{{readAllContentOfFile}}", nameSynonymous={"fileReadAll"}, description="{{functionToReadAllContentOfFile}}", params={"{{streamOfFileToRead}}"})
	protected static final Var fileReadAll(Var input) throws Exception {
		FileInputStream in = (FileInputStream)input.getObject();
		return new Var(Functions.getFileContent(in).toString());
	}

	/**
	 * Ler uma linha do arquivo
	 */
	@CronapiMetaData(type="function", name="{{readLineOfFile}}", nameSynonymous={"fileReadLine"}, description="{{functionToReadLineOfFile}}", params={"{{streamOfFileToRead}}"})
	protected static final Var fileReadLine(Var input) throws Exception {
		FileInputStream in = (FileInputStream)input.getObject();
		DataInputStream dis = new DataInputStream(in);
		String inputLine;
		if ((inputLine = dis.readLine()) != null)
			return new Var(inputLine);
		return new Var(null);
	}

	/**
	 * Limpar o arquivo
	 */
	@CronapiMetaData(type="function", name="{{clearFile}}", nameSynonymous={"fileFlush"}, description="{{functionToClearFile}}", params={"{{streamOfFileToWrite}}"})
	protected static final void fileFlush(Var input) throws Exception {
		FileOutputStream fos = (FileOutputStream)input.getObject();
		fos.flush();
	}

	/**
	 * Fechar o arquivo
	 */
	@CronapiMetaData(type="function", name="{{closeFile}}", nameSynonymous={"fileClose"}, description="{{functionToCloseFile}}", params={"{{streamOfFile}}"})
	protected static final void fileClose(Var input) throws Exception {
		if (input.getObject() instanceof  FileOutputStream) {
  		FileOutputStream fos = (FileOutputStream)input.getObject();
  		fos.flush();
  		fos.close();
		}
		else {
		  FileInputStream fis = (FileInputStream)input.getObject();
  		fis.close();
		}
	}

	/**
	 * Diretorio temporário da aplicação
	 */
	@CronapiMetaData(type="function", name="{{applicationTemporaryFolder}}", nameSynonymous={"fileTempDir"}, description="{{functionToReturnApplicationTemporaryFolder}}", params={})
	protected static final Var fileTempDir() throws Exception {
		URL location = Operations.class.getProtectionDomain().getCodeSource().getLocation();
		File tempDirectory = new File(location.getFile() + "/tmp");
		if (!tempDirectory.exists())
			tempDirectory.mkdirs();
		return new Var(tempDirectory.getAbsolutePath());
	}

	/**
	 * Ler todo conteudo do arquivo
	 */
	@CronapiMetaData(type="function", name="{{readAllContentFileInBytes}}", nameSynonymous={"fileReadAllToBytes"}, description="{{functionToReadAllContentFileInBytes}}", params={"{{streamOfFileToRead}}"})
	protected static final Var fileReadAllToBytes(Var input) throws Exception {
		FileInputStream fis = (FileInputStream)input.getObject();
		long length = fis.getChannel().size();
		byte[] bytes = new byte[(int) length];
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead = fis.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}
		if (offset < bytes.length) {
			throw new IOException("{{couldNotReadContentsFile}}");
		}
		fis.close();
		return new Var(bytes);
	}

	/**
	 * Checar se é final do arquivo
	 */
	@CronapiMetaData(type="function", name="{{isEndOfFile}}", nameSynonymous={"isFileEoF"}, description="{{functionToCheckIsEndOfFile}}", params={"{{streamOfFileToRead}}"})
	protected static final Var isFileEoF(Var input) throws Exception {
		FileInputStream fis = (FileInputStream)input.getObject();
		return new Var(fis.getChannel().position() == fis.getChannel().size());
	}

	/**
	 * Obter o tamanho do arquivo
	 */
	@CronapiMetaData(type="function", name="{{sizeOfFile}}", nameSynonymous={"fileGetSize"}, description="{{functionToGetSizeOfFile}}", params={"{{streamOfFileToRead}}"})
	protected static final Var fileGetSize(Var input) throws Exception {
		FileInputStream fis = (FileInputStream)input.getObject();
		return new Var(fis.getChannel().size());
	}

	/**
	 * Conteudo do diretorio
	 */
	@CronapiMetaData(type="function", name="{{contentOfFolder}}", nameSynonymous={"contentOfDirectory", "contentOfFolder"}, description="{{functionToGetContentOfFolder}}", params={"{{pathOfFolder}}"})
	protected static final Var contentOfDirectory(Var input) throws Exception {
		File dir = new File(input.getObjectAsString());
		List<String> filesList = new ArrayList<String>();
		String[] files = dir.list();
		if (files != null && files.length > 0) {
			for (String file : files) {
				filesList.add(dir.getAbsolutePath() + File.separator + file);
			}
		}
		return new Var(filesList);
	}

	/**
	 * É arquivo?
	 */
	@CronapiMetaData(type="function", name="{{isFile}}", nameSynonymous={"isFile"}, description="{{functionToCheckIsFile}}", params={"{{pathOfFile}}"})
	protected static final Var isFile(Var path) {
		File file = new File(path.getObjectAsString());
		return new Var(file.isFile());
	}

	/**
	 * É diretorio?
	 */
	@CronapiMetaData(type="function", name="{{isFolder}}", nameSynonymous={"isDirectory", "isFolder"}, description="{{functionToCheckIsFolder}}", params={"{{pathOfFolder}}"})
	protected static final Var isDirectory(Var path) {
		File dir = new File(path.getObjectAsString());
		return new Var(dir.isDirectory());
	}

	/**
	 * Obter Total de Linhas do Arquivo
	 */
	@CronapiMetaData(type="function", name="{{totalLinesFile}}", nameSynonymous={"fileGetNumberOfLines"}, description="{{functionToGetTotalLinesFile}}", params={"{{pathOfFile}}"})
	protected static final Var fileGetNumberOfLines(Var path) throws Exception {
		File f = new File(path.getObjectAsString());
		FileInputStream in = new FileInputStream(f);
		BufferedReader dis = new BufferedReader(new InputStreamReader(in));
		int i = 0;
		for (; dis.readLine() != null; i++);
		in.close();
		return new Var(i);
	}

	/**
	 *  Download Arquivo a partir de URL
	 */
	@CronapiMetaData(type="function", name="{{downloadFileFromUrl}}", nameSynonymous={"downloadFileFromUrl"}, description="{{functionToDownloadFileFromUrl}}", params={"{{URLAddress}}", "{{folderPathToSaveFile}}", "{{nameOfFile}}", "{{fileExtension}}"})
	public static final Var downloadFileFromUrl(Var urlAddress, Var path, Var name, Var extension) {
		try {
		  String pathLocal = path.getObjectAsString();
			java.net.URL url = new java.net.URL(urlAddress.getObjectAsString());
			if (!pathLocal.endsWith(File.separator))
			  pathLocal += pathLocal+File.separator;
			  
			java.io.InputStream is = url.openStream();
			java.io.FileOutputStream fos = new java.io.FileOutputStream(pathLocal + name.getObjectAsString() + extension.getObjectAsString());
			int umByte = 0;
			while ((umByte = is.read()) != -1) {
				fos.write(umByte);
			}
			is.close();
			fos.close();
			java.io.File file = new java.io.File(pathLocal + name.getObjectAsString()  + extension.getObjectAsString());
			return new Var(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Var(false);
	}

	/**
	 *  Ler Todo Arquivo Definindo Charset	
	 */
	@CronapiMetaData(type="function", name="{{readAllFileWithCharset}}", nameSynonymous={"fileReadContentWithCharset"}, description="{{functionToReadAllFileWithCharset}}", params={"{{streamOfFileToRead}}", "{{charset}}"})
	protected static final Var fileReadContentWithCharset(Var finp, Var charsetSelected) throws Exception {
		StringBuilder r = new StringBuilder();

    String charset = charsetSelected.getObjectAsString();
    FileInputStream fstream = (FileInputStream)finp.getObject();
		DataInputStream in = null;
		try {
			in = new DataInputStream(fstream);
			byte[] b = new byte[254];
			while (in.available() != 0) {
				int bytes = in.read(b);
				r.append(new String(b, 0, bytes, charset));
			}
		} catch (Exception e) {
			e.printStackTrace();
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
		return new Var(r.toString());
	}
	
	/**
	 *  Descompactar arquivo zip	
	 */
	@CronapiMetaData(type="function", name="{{unZipFile}}", nameSynonymous={"fileReadContentWithCharset"}, description="{{functionToUnZipFile}}", params={"{{streamOfFileToRead}}", "{{destinationFolder}}"})
	protected static void unZipItApache(Var zippedFile, Var destFolder) throws Exception {
    FileInputStream zipFile = (FileInputStream)zippedFile.getObject();
    String outputFolder = destFolder.getObjectAsString();
    if (!outputFolder.endsWith("/")) {
      outputFolder += "/";
    }
    byte[] buffer = new byte[1024];
    org.apache.commons.compress.archivers.zip.ZipArchiveInputStream zis = new org.apache.commons.compress.archivers.zip.ZipArchiveInputStream(zipFile, "UTF-8", true);
    org.apache.commons.compress.archivers.zip.ZipArchiveEntry ze = zis.getNextZipEntry();
    java.nio.charset.Charset utf8charset = java.nio.charset.Charset.forName("UTF-8");
    java.nio.charset.Charset iso88591charset = java.nio.charset.Charset.forName("ISO-8859-1");
    while (ze != null) {
      String fileName = ze.getName();
      java.io.File newFile = new java.io.File(outputFolder + fileName);
      if (ze.isDirectory()) {
        new java.io.File(outputFolder + fileName).mkdirs();
      } else {
        new java.io.File(newFile.getParent()).mkdirs();
        java.io.FileOutputStream fos = new java.io.FileOutputStream(newFile);
        int len;
        while ((len = zis.read(buffer)) > 0) {
          if (newFile.getAbsolutePath().endsWith(".js") || newFile.getAbsolutePath().endsWith(".html") || newFile.getAbsolutePath().endsWith(".htm")) {
            java.nio.ByteBuffer inputBuffer = java.nio.ByteBuffer.wrap(buffer, 0, len);
            java.nio.CharBuffer data = utf8charset.decode(inputBuffer);
            java.nio.ByteBuffer outputBuffer = iso88591charset.encode(data);
            fos.write(outputBuffer.array(), 0, outputBuffer.array().length);
          } else {
            fos.write(buffer, 0, len);
          }
        }
        fos.close();
      }
      ze = zis.getNextZipEntry();
    }
    zis.close();
  }
}