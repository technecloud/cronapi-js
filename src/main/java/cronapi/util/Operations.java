package cronapi.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import cronapi.CronapiMetaData;
import cronapi.Var;

/**
 * Classe que representa ...
 * 
 * @author Rodrigo Reis
 * @version 1.0
 * @since 2017-03-31
 *
 */
@CronapiMetaData(category = "Util", categorySynonymous = { "Util", "Operations" })
public class Operations {

	/**
	 * Construtor
	 **/
	public Operations() {
	}

	// Copiar para área de transferência	
	@CronapiMetaData(type = "function", name = "Copia texto para área de transferência", categorySynonymous = "copyTextToTransferArea", description = "Função que copia o texto para área de transferência", params = {
			"Varíavel a ser copiada na área de transferência" })
	public static final void copyTextToTransferArea(Var strVar) throws Exception {
		String str = strVar.getObjectAsString();
		java.awt.datatransfer.Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
		java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(str);
		clipboard.setContents(selection, null);
	}

	@CronapiMetaData(type = "function", name = "Executa linha de comando.", categorySynonymous = "shellExecute  ", description = "Função que executa uma linha de comando e retoan caso haja erro.", params = {
			"Varíavel a ser executada", "Variavel que define se deve coletar e retornar o erro." })
	public static final Var shellExecute(Var cmdline, Var waitFor) throws Exception {
		Boolean waitForCasted = (Boolean) waitFor.getObject();
		Process p = Runtime.getRuntime().exec(cmdline.getObjectAsString());
		if (waitForCasted) {
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String r = "";
			String line;
			while ((line = input.readLine()) != null) {
				r += (line + "\n");
			}
			input.close();
			return new Var(r);
		}
		return new Var();
	}

	// Retorna um numério aleatório 
	@CronapiMetaData(type = "function", name = "Retorna um número aleatório.", categorySynonymous = "random", description = "Função que retorna um inteiro positivo aleatório com valor máximo indicado no parâmetro.", params = {
			"Varíavel que indica o valor máximo do random" })
	public static final Var random(Var maxValue) throws Exception {
		return new Var(Math.round(Math.random() * maxValue.getObjectAsDouble()));
	}

	@CronapiMetaData(type = "function", name = "Comprime array de bytes.", categorySynonymous = "compressToZip", description = "Função que comprime um var para zip.", params = {
			"Varíavel a ser comprimida" })
	public static final Var compressToZip(Var value) throws Exception {
		java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
		java.util.zip.DeflaterOutputStream compresser = new java.util.zip.DeflaterOutputStream(output);
		compresser.write((byte[]) value.getObject());
		compresser.finish();
		compresser.close();
		return new Var(output.toByteArray());
	}


	@CronapiMetaData(type = "function", name = "Descompacta array de bytes.", categorySynonymous = "decodeZipFromByte", description = "Função que descompacta de zip para array de bytes.", params = {
			"Varíavel a ser descompactada." })
	public static final Var decodeZipFromByte(Var value) throws Exception {
		java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream((byte[]) value.getObject());
		java.util.zip.InflaterInputStream decompresser = new java.util.zip.InflaterInputStream(input);
		byte[] buffer = new byte[1024 * 4];//4KB
		java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
		int len;
		while ((len = decompresser.read(buffer)) != -1) {
			out.write(buffer, 0, len);
		}
		decompresser.close();
		out.close();
		input.close();
		return new Var(out.toByteArray());
	}
	
	
}
