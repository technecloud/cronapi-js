package cronapi.conversion;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import cronapi.CronapiMetaData;
import cronapi.Functions;
import cronapi.Var;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;
import cronapi.i18n.Messages;

/**
 * Classe que representa ...
 * 
 * @author Usuário de Teste
 * @version 1.0
 * @since 2017-03-29
 *
 */
@CronapiMetaData(category = CategoryType.CONVERSION, categoryTags = { "Conversão", "Convert" })
public class Operations {

	@CronapiMetaData(type = "function", name = "{{toLogic}}", nameTags = {
			"toBoolean" }, description = "{{functionConvertToLogic}}", params = {
					"{{content}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.BOOLEAN)
	protected final Var toBoolean(Var var) throws Exception {
		return new Var(Functions.stringToBoolean(var.getObjectAsString()));
	}

	@CronapiMetaData(type = "function", name = "{{base64ToText}}", nameTags = {
			"base64ToString" }, description = "{{functionConvertBase64ToText}}", params = {
					"{{contentInBase64}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.OBJECT)
	protected static final Var base64ToString(Var base64Var) throws Exception {
		Var value = new Var();
		String base64 = base64Var.getObjectAsString();
		if (base64.length() > 0) {
			value = new Var(Base64.getDecoder().decode(base64));
		}
		return value;
	}

	@CronapiMetaData(type = "function", name = "{{textToTextBinary}}", nameTags = {
			"asciiToBinary" }, description = "{{functionToConvertTextInTextBinary}}", params = {
					"{{contentInAscii}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.STRING)
	protected static final Var asciiToBinary(Var asciiVar) throws Exception {
		StringBuilder binary = new StringBuilder();
		String ascii = asciiVar.getObjectAsString();
		if (ascii.length() > 0) {
			for (int i = 0; i < ascii.length(); i++) {
				int value = ascii.charAt(i);
				for (int j = 7; j >= 0; j--) {
					binary.append((value >> j) & 1);
				}
			}
		}
		return new Var(binary.toString());
	}

	@CronapiMetaData(type = "function", name = "{{textBinaryToText}}", nameTags = {
			"binaryToAscii" }, description = "{{functionToConvertTextBinaryToText}}", params = {
					"{{contentInTextBinary}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.STRING)
	protected static final Var binaryToAscii(Var binaryVar) throws Exception {
		StringBuilder ascii = new StringBuilder();
		String binary = binaryVar.getObjectAsString();
		if (binary.length() > 0) {
			if ((binary.length()) % 8 != 0) {
				throw new Exception(Messages.getString("parameterNotBinary"));
			}
			for (int i = 0; i < binary.length(); i += 8) {
				String temp = binary.substring(i, i + 8);
				int decimal = 0;
				for (int j = 0; j < 8; j++) {
					decimal += ((temp.charAt(7 - j) == '1') ? (1 << j) : 0);
				}
				ascii.append((char) decimal);
			}
		}
		return new Var(ascii.toString());
	}

	@CronapiMetaData(type = "function", name = "{{convertToBytes}}", nameTags = {
			"toBytes" }, description = "{{functionToConvertTextBinaryToText}}", params = {
					"{{contentInTextBinary}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.OBJECT)
	protected static final Var toBytes(Var var) throws Exception {
		return new Var(var.getObjectAsString().getBytes());
	}

	@CronapiMetaData(type = "function", name = "{{convertToAscii}}", nameTags = { "chrToAscii",
			"convertToAscii" }, description = "{{functionToConvertToAscii}}", params = {
					"{{content}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.FLOAT)
	protected static final Var chrToAscii(Var value) throws Exception {
		Var ascii = new Var(null);
		if (value.getObjectAsString() != null && value.getObjectAsString().isEmpty()) {
			ascii = new Var(Long.valueOf(value.getObjectAsString().charAt(0)));
		}
		return ascii;
	}

	@CronapiMetaData(type = "function", name = "{{convertHexadecimalToInt}}", nameTags = { "hexToInt",
			"hexadecimalToInteger" }, description = "{{functionToConvertHexadecimalToInt}}", params = {
					"{{content}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.INTEGER)
	protected static final Var hexToInt(Var value) {
		return new Var(Integer.valueOf(value.getObjectAsString(), 16).intValue());
	}

	@CronapiMetaData(type = "function", name = "{{convertArrayToList}}", nameTags = {
			"arrayToList" }, description = "{{functionToConvertArrayToList}}", params = {
					"{{content}}" }, paramsType = { ObjectType.LIST }, returnType = ObjectType.LIST)
	protected static final Var arrayToList(Var arrayVar) throws Exception {
		Object array = arrayVar.getObject();
		if (array instanceof byte[]) {
			byte[] bytes = (byte[]) array;
			List l = new ArrayList(bytes.length);
			for (int i = 0; i < bytes.length; i++) {
				l.add(bytes[i]);
			}
			return new Var(l);
		}
		List l = new ArrayList(java.lang.reflect.Array.getLength(array));
		for (int i = 0; i < java.lang.reflect.Array.getLength(array); i++) {
			l.add(java.lang.reflect.Array.get(array, i));
		}
		return new Var(l);
	}

	@CronapiMetaData(type = "function", name = "{{convertBase64ToBinary}}", nameTags = {
			"base64ToBinary" }, description = "{{functionToConvertBase64ToBinary}}", params = {
					"{{content}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.OBJECT)
	protected static final Var base64ToBinary(Var base64) throws Exception {
		Var binary = new Var(null);
		byte[] temp = Functions.getFromBase64(base64.getObjectAsString());
		if (temp != null)
			binary = new Var(temp);
		return binary;
	}

	@CronapiMetaData(type = "function", name = "{{convertStringToJs}}", nameTags = {
			"stringToJs" }, description = "{{functionToConvertStringToJs}}", params = {
					"{{content}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.STRING)
	protected static final Var stringToJs(Var val) throws Exception {
		return new Var(Functions.stringToJs(val.getObjectAsString()));
	}
}
