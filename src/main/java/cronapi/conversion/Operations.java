package cronapi.conversion;

import java.util.Base64;

import cronapi.CronapiMetaData;
import cronapi.Functions;
import cronapi.Var;
import cronapi.i18n.Messages;

/**
 * Classe que representa ...
 * 
 * @author Usuário de Teste
 * @version 1.0
 * @since 2017-03-29
 *
 */
@CronapiMetaData(category="Conversion", categorySynonymous={"Conversão", "Convert"})
public class Operations {
 
  @CronapiMetaData(type="function", name="{{toLogic}}", nameSynonymous={"toBoolean"}, description="{{functionConvertToLogic}}", params={"{{content}}"})
  protected final Var toBoolean(Var var) throws Exception {
    return new Var(Functions.stringToBoolean(var.getObjectAsString()));
  }
  
  @CronapiMetaData(type="function", name="{{base64ToText}}", nameSynonymous={"base64ToString"}, description="{{functionConvertBase64ToText}}", params={"{{contentInBase64}}"})
  protected static final Var base64ToString(Var base64Var) throws Exception {
    Var value = new Var();
    String base64 = base64Var.getObjectAsString();
    if (base64.length() > 0) {
      value = new Var(Base64.getDecoder().decode(base64));
    }
    return value;
  }
  
  @CronapiMetaData(type="function", name="{{textToTextBinary}}", nameSynonymous={"asciiToBinary"}, description="{{functionToConvertTextInTextBinary}}", params={"{{contentInAscii}}"})
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
  
  @CronapiMetaData(type="function", name="{{textBinaryToText}}", nameSynonymous={"binaryToAscii"}, description="{{functionToConvertTextBinaryToText}}", params={"{{contentInTextBinary}}"})
  protected static final Var binaryToAscii(Var binaryVar) throws Exception {
    StringBuilder ascii = new StringBuilder();
    String binary = binaryVar.getObjectAsString();
    if (binary.length() > 0) {
      if ((binary.length()) % 8 != 0) {
        throw new Exception(Messages.getString("parameterNotBinary"));
      }
      for (int i = 0; i < binary.length(); i+=8) {
         String temp = binary.substring(i, i+8);
         int decimal = 0;
         for (int j = 0; j < 8; j++) {
           decimal += ( (temp.charAt(7 - j) == '1') ? (1 << j) : 0 );
         }
         ascii.append((char) decimal);
      }
    }
    return new Var(ascii.toString());
  }
  
  @CronapiMetaData(type="function", name="{{convertToBytes}}", nameSynonymous={"toBytes"}, description="{{functionToConvertTextBinaryToText}}", params={"{{contentInTextBinary}}"})
  protected final Var toBytes(Var var) throws Exception {
    return new Var(var.getObjectAsString().getBytes());
  }

}
