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
	@CronapiMetaData(type="function", name="Criar pasta", nameSynonymous={"createFolder"}, description="Função para criar nova pasta", params={"Caminho que deve ser criado a pasta"})
	protected static final boolean folderCreate(String path) throws Exception {
		File file = new File(path.trim());
		return folderCreate(file);
	}

	/**
	 * Criar nova pasta 
	 */
	@CronapiMetaData(type="function", name="Criar pasta", nameSynonymous={"createFolder"},description="Função para criar nova pasta", params={"Caminho que deve ser criado a pasta"})
	protected static final boolean folderCreate(File dir) throws Exception {
		boolean created = true;
		if (!dir.exists()) {
			created = dir.mkdirs();
		}
		return created;
	}

	/**
	 * MD5 do Arquivo
	 */
	@CronapiMetaData(type="function", name="MD5 do arquivo", nameSynonymous={"fileMD5"},description="Função para retornar o MD5 do arquivo", params={"Caminho do arquivo"})
	protected static final String fileMD5(String path) throws Exception {
		return Functions.MD5AsStringFromFile(new File(path));
	}

	/**
	 * Remover Pasta de Arquivos
	 */
	@CronapiMetaData(type="function", name="Remover pasta de arquivos", nameSynonymous={"removeFolder", "deleteFolder"}, description="Função para remover pasta de arquivos", params={"Caminho da pasta"})
	protected static final boolean fileRemoveFolderAndChildren(String path) throws Exception {
		File dir = new File(path.trim());
		return fileRemoveFolderAndChildren(dir);
	}

	/**
	 * Remover Pasta de Arquivos
	 */
	@CronapiMetaData(type="function", name="Remover pasta de arquivos", nameSynonymous={"removeFolder", "deleteFolder"}, description="Função para remover pasta de arquivos", params={"Pasta"})
	protected static final boolean fileRemoveFolderAndChildren(File dir) throws Exception {
		return Functions.deleteFolder(dir);
	}

	/**
	 * Obter MD5 do Arquivo
	 */
	@CronapiMetaData(type="function", name="Obter MD5 do arquivo", nameSynonymous={"getFileMD5"}, description="Função para obter MD5 do arquivo", params={"Caminho do arquivo"})
	protected static final String getFileMD5(String filename) throws Exception {
		java.io.File file = new java.io.File(filename);
		return getFileMD5(file);
	}

	/**
	 * Obter MD5 do Arquivo
	 */
	@CronapiMetaData(type="function", name="Obter MD5 do Arquivo", nameSynonymous={"getFileMD5"}, description="Função para obter MD5 do arquivo", params={"Arquivo"})
	protected static final String getFileMD5(File file) throws Exception {
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
			return result;
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
	@CronapiMetaData(type="function", name="Pode ler arquivo?", nameSynonymous={"fileCanRead"}, description="Função para verificar se pode ler o arquivo", params={"Caminho do arquivo"})
	protected final boolean fileCanRead(String path) throws Exception {
		File file = new File(path.trim());
		return fileCanRead(file);
	}

	/**
	 * Pode Ler?
	 */
	@CronapiMetaData(type="function", name="Pode ler arquivo?", nameSynonymous={"fileCanRead"}, description="Função para verificar se pode ler o arquivo", params={"Arquivo"})
	protected final boolean fileCanRead(File file) throws Exception {
		return file.canRead();
	}

	/**
	 * Pode Escrever?
	 */
	@CronapiMetaData(type="function", name="Pode escrever arquivo?", nameSynonymous={"fileCanWrite"}, description="Função para verificar se pode escrever o arquivo", params={"Caminho do arquivo"})
	protected final boolean fileCanWrite(String path) throws Exception {
		File file = new File(path.trim());
		return fileCanWrite(file);
	}

	/**
	 * Pode Escrever?
	 */
	@CronapiMetaData(type="function", name="Pode escrever arquivo?", nameSynonymous={"fileCanWrite"}, description="Função para verificar se pode escrever o arquivo", params={"Arquivo"})
	protected final boolean fileCanWrite(File file) throws Exception {
		return file.canWrite();
	}

	/**
	 * Criar Novo Arquivo
	 */
	@CronapiMetaData(type="function", name="Criar novo arquivo", nameSynonymous={"fileCreate"}, description="Função para criar arquivo", params={"Caminho do arquivo"})
	protected static final void fileCreate(String path) throws Exception {
		File file = new File(path.trim());
		fileCreate(file);
	}

	/**
	 * Criar Novo Arquivo
	 */
	@CronapiMetaData(type="function", name="Criar novo arquivo", nameSynonymous={"fileCreate"}, description="Função para criar arquivo", params={"Arquivo"})
	protected static final void fileCreate(File file) throws Exception {
		if (!file.exists()) {
			OutputStream out = new FileOutputStream(file);
			out.close();
		}
	}

	/**
	 * Remover Arquivo
	 */
	@CronapiMetaData(type="function", name="Remover arquivo", nameSynonymous={"fileRemove"}, description="Função para remover arquivo", params={"Caminho do arquivo"})
	protected static final boolean fileRemove(String path) throws Exception {
		File file = new File(path.trim());
		return fileRemove(file);
	}

	/**
	 * Remover Arquivo
	 */
	@CronapiMetaData(type="function", name="Remover arquivo", nameSynonymous={"fileRemove"}, description="Função para remover arquivo", params={"Arquivo"})
	protected static final boolean fileRemove(File file) throws Exception {
		return file.delete();
	}

	/**
	 * Existe o Arquivo?
	 */
	@CronapiMetaData(type="function", name="Existe arquivo?", nameSynonymous={"fileExists"}, description="Função para verificar se existe o arquivo", params={"Caminho do arquivo"})
	protected static final boolean fileExists(String path) throws Exception {
		File file = new File(path.trim());
		return fileExists(file);
	}

	/**
	 * Existe o Arquivo?
	 */
	@CronapiMetaData(type="function", name="Existe arquivo?", nameSynonymous={"fileExists"}, description="Função para verificar se existe o arquivo", params={"Arquivo"})
	protected static final boolean fileExists(File file) throws Exception {
		return file.exists();
	}

	/**
	 * Copiar Arquivo
	 */
	@CronapiMetaData(type="function", name="Copiar arquivo", nameSynonymous={"fileCopy"}, description="Função para copiar o arquivo", params={"Caminho de origem", "Caminho de destino"})
	protected static final void fileCopy(String pathFrom, String pathTo) throws Exception {
		File from = new File(pathFrom.trim());
		File to = new File(pathTo.trim());
		fileCopy(from, to);
	}

	/**
	 * Copiar Arquivo
	 */
	@CronapiMetaData(type="function", name="Copiar arquivo", nameSynonymous={"fileCopy"}, description="Função para copiar o arquivo", params={"Arquivo de origem", "Pasta de destino"})
	protected static final void fileCopy(File pathFrom, File pathTo) throws Exception {
		Functions.copyFileTo(pathFrom, pathTo);
	}

	/**
	 * Obter Pai do Arquivo
	 */
	@CronapiMetaData(type="function", name="Obter pai do arquivo", nameSynonymous={"fileGetParent"}, description="Função para obter o pai do arquivo", params={"Caminho do arquivo"})
	protected static final String fileGetParent(String path) throws Exception {
		File file = new File(path.trim());
		return fileGetParent(file);
	}

	/**
	 * Obter Pai do Arquivo
	 */
	@CronapiMetaData(type="function", name="Obter pai do arquivo", nameSynonymous={"fileGetParent"}, description="Função para obter o pai do arquivo", params={"Arquivo"})
	protected static final String fileGetParent(File file) throws Exception {
		if (file.exists()) {
			return file.getParent();
		} else {
			return null;
		}
	}

	/**
	 * Renomear Arquivo
	 */
	@CronapiMetaData(type="function", name="Renomear arquivo", nameSynonymous={"fileRename"}, description="Função para renomear arquivo", params={"Caminho do arquivo", "Novo nome do arquivo"})
	protected static final boolean fileRename(String path, String name) throws Exception {
		File from = new File(path.trim());
		return fileRename(from, name);
	}

	/**
	 * Renomear Arquivo
	 */
	@CronapiMetaData(type="function", name="Renomear arquivo", nameSynonymous={"fileRename"}, description="Função para renomear arquivo", params={"Arquivo", "Novo nome do arquivo"})
	protected static final boolean fileRename(File from, String name) throws Exception {
		File to = new File(from.getParentFile(), name.trim());
		return from.renameTo(to);
	}

	/**
	 * Mover Arquivo
	 */
	@CronapiMetaData(type="function", name="Mover arquivo", nameSynonymous={"fileMove"}, description="Função para mover arquivo", params={"Caminho de origem do arquivo", "Caminho de destino do arquivo"})
	protected static final boolean fileMove(String pathFrom, String pathTo) throws Exception {
		File from = new File(pathFrom.trim());
		return fileMove(from, pathTo);
	}

	/**
	 * Mover Arquivo
	 */
	@CronapiMetaData(type="function", name="Mover arquivo", nameSynonymous={"fileMove"}, description="Função para mover arquivo", params={"Arquivo", "Caminho de destino do arquivo"})
	protected static final boolean fileMove(File from, String pathTo) throws Exception {
		File to = new File(pathTo.trim());
		return from.renameTo(to);
	}

	/**
	 * Forçar criação do diretorio para o arquivo
	 */
	@CronapiMetaData(type="function", name="Forçar criação da pasta para o arquivo", nameSynonymous={"forceDirectories", "forceFolder", "forceCreateDirectories", "forceCreateFolder"}, description="Função para forçar criação da pasta para o arquivo", params={"Caminho do arquivo"})
	protected static final boolean forceDirectories(String path) throws Exception {
		return new File(path).getParentFile().mkdirs();
	}

	/**
	 * Forçar criação do diretorio para o arquivo
	 */
	@CronapiMetaData(type="function", name="Forçar criação da pasta para o arquivo", nameSynonymous={"forceDirectories", "forceFolder", "forceCreateDirectories", "forceCreateFolder"}, description="Função para forçar criação da pasta para o arquivo", params={"Arquivo"})
	protected static final boolean forceDirectories(File file) throws Exception {
		return file.getParentFile().mkdirs();
	}

	/**
	 * Criar pasta
	 */
	@CronapiMetaData(type="function", name="Criar pasta", nameSynonymous={"createFolder", "createDirectory"}, description="Função para criar pasta", params={"Caminho da pasta"})
	protected static final boolean createFolder(String absolutePath) throws Exception {
		return new File(absolutePath).mkdirs();
	}

	/**
	 * Abrir arquivo para escrita
	 */
	@CronapiMetaData(type="function", name="Abrir arquivo para escrita", nameSynonymous={"fileOpenToWrite"}, description="Função para abrir arquivo para escrita", params={"Caminho do arquivo", "Adicionar conteudo?"})
	protected static final FileOutputStream fileOpenToWrite(String url, boolean append) throws Exception {
		FileOutputStream out = new FileOutputStream(new File(url), append);
		return out;
	}

	/**
	 * Abrir arquivo para escrita
	 */
	@CronapiMetaData(type="function", name="Abrir arquivo para escrita", nameSynonymous={"fileOpenToWrite"}, description="Função para abrir arquivo para escrita", params={"Arquivo", "Adicionar conteudo?"})
	protected static final FileOutputStream fileOpenToWrite(File file, boolean append) throws Exception {
		FileOutputStream out = new FileOutputStream(file, append);
		return out;
	}

	/**
	 * Abrir arquivo para leitura
	 */
	@CronapiMetaData(type="function", name="Abrir arquivo para leitura", nameSynonymous={"fileOpenToRead"}, description="Função para abrir arquivo para leitura", params={"Caminho do arquivo"})
	protected static final FileInputStream fileOpenToRead(String url) throws Exception {
		FileInputStream in = new FileInputStream(new File(url));
		return in;
	}

	/**
	 * Adicionar contéudo a arquivo
	 */
	@CronapiMetaData(type="function", name="Adicionar contéudo a arquivo", nameSynonymous={"fileAppend"}, description="Função para adicionar contéudo a arquivo", params={"Stream do arquivo", "Conteúdo do arquivo"})
	protected static final void fileAppend(FileOutputStream out, byte[] content) throws Exception {
		out.write(content);
	}

	/**
	 * Adicionar contéudo a arquivo
	 */
	@CronapiMetaData(type="function", name="Adicionar contéudo a arquivo", nameSynonymous={"fileAppend"}, description="Função para adicionar contéudo a arquivo", params={"Stream do arquivo", "Conteúdo do arquivo"})
	protected static final void fileAppend(FileOutputStream out, String content) throws Exception {
		out.write(content.getBytes());
	}

	/**
	 * Ler contéudo do arquivo
	 */
	@CronapiMetaData(type="function", name="Ler conteúdo do arquivo", nameSynonymous={"fileRead"}, description="Função para ler conteúdo do arquivo", params={"Stream do arquivo", "Tamanho"})
	protected static final String fileRead(FileInputStream in, Long size) throws Exception {
		byte[] b = new byte[size.intValue()];
		if (in.available() != 0) {
			int bytes = in.read(b);
			return new String(b, 0, bytes);
		}
		return null;
	}

	/**
	 * Ler todo contéudo do arquivo
	 */
	@CronapiMetaData(type="function", name="Ler todo conteúdo do arquivo", nameSynonymous={"fileReadAll"}, description="Função para ler todo conteúdo do arquivo", params={"Stream do arquivo"})
	protected static final StringBuffer fileReadAll(FileInputStream in) throws Exception {
		return Functions.getFileContent(in);
	}

	/**
	 * Ler uma linha do arquivo
	 */
	@CronapiMetaData(type="function", name="Ler linha do arquivo", nameSynonymous={"fileReadLine"}, description="Função para ler linha do arquivo", params={"Stream do arquivo"})
	protected static final String fileReadLine(FileInputStream in) throws Exception {
		DataInputStream dis = new DataInputStream(in);
		String inputLine;
		if ((inputLine = dis.readLine()) != null)
			return inputLine;
		return null;
	}

	/**
	 * Limpar o arquivo
	 */
	@CronapiMetaData(type="function", name="Limpar arquivo", nameSynonymous={"fileFlush"}, description="Função para limpar arquivo", params={"Stream do arquivo"})
	protected static final void fileFlush(FileOutputStream fos) throws Exception {
		fos.flush();
	}

	/**
	 * Fechar o arquivo
	 */
	@CronapiMetaData(type="function", name="Fechar o arquivo", nameSynonymous={"fileClose"}, description="Função para fechar o arquivo", params={"Stream do arquivo"})
	protected static final void fileClose(FileOutputStream fos) throws Exception {
		fos.flush();
		fos.close();
	}

	/**
	 * Fechar o arquivo
	 */
	@CronapiMetaData(type="function", name="Fechar o arquivo", nameSynonymous={"fileClose"}, description="Função para fechar o arquivo", params={"Stream do arquivo"})
	protected static final void fileClose(FileInputStream fis) throws Exception {
		fis.close();
	}

	/**
	 * Diretorio temporário da aplicação
	 */
	@CronapiMetaData(type="function", name="Diretorio temporário da aplicação", nameSynonymous={"fileTempDir"}, description="Função para retornar diretorio temporário da aplicação", params={})
	protected static final String fileTempDir() throws Exception {
		URL location = Operations.class.getProtectionDomain().getCodeSource().getLocation();
		File tempDirectory = new File(location.getFile() + "/tmp");
		if (!tempDirectory.exists())
			tempDirectory.mkdirs();
		return tempDirectory.getAbsolutePath();
	}

	/**
	 * Ler todo conteudo do arquivo
	 */
	@CronapiMetaData(type="function", name="Ler todo conteúdo do arquivo em bytes", nameSynonymous={"fileReadAllToBytes"}, description="Função para ler todo conteúdo do arquivo em bytes", params={"Stream do arquivo"})
	protected static final byte[] fileReadAllToBytes(FileInputStream fis) throws Exception {
		long length = fis.getChannel().size();
		byte[] bytes = new byte[(int) length];
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead = fis.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}
		if (offset < bytes.length) {
			throw new IOException("Could not read entire contents of file");
		}
		fis.close();
		return bytes;
	}

	/**
	 * Checar se é final do arquivo
	 */
	@CronapiMetaData(type="function", name="É final do arquivo?", nameSynonymous={"isFileEoF"}, description="Função para verificar se é final do arquivo", params={"Stream do arquivo"})
	protected static final boolean isFileEoF(FileInputStream fis) throws Exception {
		return fis.getChannel().position() == fis.getChannel().size();
	}

	/**
	 * Obter o tamanho do arquivo
	 */
	@CronapiMetaData(type="function", name="Tamanho do arquivo", nameSynonymous={"fileGetSize"}, description="Função para obter o tamanho do arquivo", params={"Stream do arquivo"})
	protected static final long fileGetSize(FileInputStream fis) throws Exception {
		return fis.getChannel().size();
	}

	/**
	 * Conteudo do diretorio
	 */
	@CronapiMetaData(type="function", name="Conteúdo da pasta", nameSynonymous={"contentOfDirectory", "contentOfFolder"}, description="Função para obter o conteúdo da pasta", params={"Pasta"})
	protected static final List<String> contentOfDirectory(File dir) throws Exception {
		List<String> filesList = new ArrayList<String>();
		String[] files = dir.list();
		if (files != null && files.length > 0) {
			for (String file : files) {
				filesList.add(dir.getAbsolutePath() + File.separator + file);
			}
		}
		return filesList;
	}

	/**
	 * É arquivo?
	 */
	@CronapiMetaData(type="function", name="É arquivo?", nameSynonymous={"isFile"}, description="Função verificar se é arquivo", params={"Caminho do arquivo"})
	protected static final boolean isFile(String path) {
		File file = new File(path);
		return isFile(file);
	}

	/**
	 * É arquivo?
	 */
	@CronapiMetaData(type="function", name="É arquivo?", nameSynonymous={"isFile"}, description="Função verificar se é arquivo", params={"Arquivo"})
	protected static final boolean isFile(File file) {
		return file.isFile();
	}

	/**
	 * É diretorio?
	 */
	@CronapiMetaData(type="function", name="É pasta?", nameSynonymous={"isDirectory", "isFolder"}, description="Função verificar se é pasta", params={"Caminho da pasta"})
	protected static final boolean isDirectory(String path) {
		File dir = new File(path);
		return isDirectory(dir);
	}

	/**
	 * É diretorio?
	 */
	@CronapiMetaData(type="function", name="É pasta?", nameSynonymous={"isDirectory", "isFolder"}, description="Função verificar se é pasta", params={"Pasta"})
	protected static final boolean isDirectory(File dir) {
		return dir.isDirectory();
	}

	/**
	 * Obter Total de Linhas do Arquivo
	 */
	@CronapiMetaData(type="function", name="Total linhas arquivo", nameSynonymous={"fileGetNumberOfLines"}, description="Função para obter o total de linhas do arquivo", params={"Caminho do arquivo"})
	protected static final int fileGetNumberOfLines(String path) throws Exception {
		File f = new File(path);
		return fileGetNumberOfLines(f);
	}

	/**
	 * Obter Total de Linhas do Arquivo
	 */
	@CronapiMetaData(type="function", name="Total linhas arquivo", nameSynonymous={"fileGetNumberOfLines"}, description="Função para obter o total de linhas do arquivo", params={"Arquivo"})
	protected static final int fileGetNumberOfLines(File f) throws Exception {
		FileInputStream in = new FileInputStream(f);
		BufferedReader dis = new BufferedReader(new InputStreamReader(in));
		int i = 0;
		for (; dis.readLine() != null; i++)
			;
		in.close();
		return i;
	}

	/**
	 *  Download Arquivo a partir de URL
	 */
	@CronapiMetaData(type="function", name="Download arquivo a partir de URL", nameSynonymous={"downloadFileFromUrl"}, description="Função para fazer download de arquivo a partir de URL", params={"Endereço url", "Caminho da pasta para salvar o arquivo", "Nome do arquivo", "Extensão do arquivo"})
	public static final boolean downloadFileFromUrl(String urlAddress, String pathLocal, String name, String extension) {
		try {
			java.net.URL url = new java.net.URL(urlAddress);
			if (!pathLocal.endsWith(File.separator))
			  pathLocal += pathLocal+File.separator;
			  
			java.io.InputStream is = url.openStream();
			java.io.FileOutputStream fos = new java.io.FileOutputStream(pathLocal + name + extension);
			int umByte = 0;
			while ((umByte = is.read()) != -1) {
				fos.write(umByte);
			}
			is.close();
			fos.close();
			java.io.File file = new java.io.File(pathLocal + name  + extension);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 *  Ler Todo Arquivo Definindo Charset	
	 */
	@CronapiMetaData(type="function", name="Ler todo arquivo definindo Charset", nameSynonymous={"fileReadContentWithCharset"}, description="Função para ler todo arquivo definindo Charset", params={"Stream do arquivo", "Charset"})
	protected static final StringBuilder fileReadContentWithCharset(FileInputStream fstream, String charset) throws Exception {
		StringBuilder r = new StringBuilder();

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
		return r;
	}
}