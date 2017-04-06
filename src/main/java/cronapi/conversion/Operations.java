package cronapi.conversion;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import cronapi.CronapiMetaData;
import cronapi.Utils;
import cronapi.Var;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;

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
		return new Var(Utils.stringToBoolean(var.getObjectAsString()));
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
		byte[] bytes = asciiVar.getObjectAsString().getBytes();
    StringBuilder binary = new StringBuilder();
    for (byte b : bytes)
    {
       int val = b;
       for (int i = 0; i < 8; i++)
       {
          binary.append((val & 128) == 0 ? 0 : 1);
          val <<= 1;
       }
    }
    return new Var(binary.toString());
	}

	@CronapiMetaData(type = "function", name = "{{textBinaryToText}}", nameTags = {
			"binaryToAscii" }, description = "{{functionToConvertTextBinaryToText}}", params = {
					"{{contentInTextBinary}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.STRING)
	protected static final Var binaryToAscii(Var binaryVar) throws Exception {
		StringBuilder sb = new StringBuilder();
    char[] chars = binaryVar.getObjectAsString().replaceAll("\\s", "").toCharArray();
    for(int j = 0; j < chars.length; j += 8) {
      int idx = 0;
      int sum = 0;
      for(int i = 7; i >= 0; i--) {
        if(chars[i + j] == '1') {
          sum += 1 << idx;
        }
        idx++;
      }
      sb.append(Character.toChars(sum));
    }
    return new Var(sb.toString());
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
		return new Var(Integer.parseInt(value.getObjectAsString(), 16));
	}

	@CronapiMetaData(type = "function", name = "{{convertArrayToList}}", nameTags = {
			"arrayToList" }, description = "{{functionToConvertArrayToList}}", params = {
					"{{content}}" }, paramsType = { ObjectType.LIST }, returnType = ObjectType.LIST)
	protected static final Var arrayToList(Var arrayVar) throws Exception {
		List<?> t = Arrays.asList(arrayVar.getObject());
		return new Var(t);
	}

	@CronapiMetaData(type = "function", name = "{{convertBase64ToBinary}}", nameTags = {
			"base64ToBinary" }, description = "{{functionToConvertBase64ToBinary}}", params = {
					"{{content}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.OBJECT)
	protected static final Var base64ToBinary(Var base64) throws Exception {
		Var binary = new Var(null);
		byte[] temp = Utils.getFromBase64(base64.getObjectAsString());
		if (temp != null)
			binary = new Var(temp);
		return binary;
	}

	@CronapiMetaData(type = "function", name = "{{convertStringToJs}}", nameTags = {
			"stringToJs" }, description = "{{functionToConvertStringToJs}}", params = {
					"{{content}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.STRING)
	protected static final Var stringToJs(Var val) throws Exception {
		return new Var(Utils.stringToJs(val.getObjectAsString()));
	}
}
