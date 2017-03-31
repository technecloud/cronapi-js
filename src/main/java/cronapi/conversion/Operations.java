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
 
  @CronapiMetaData(type="function", name="{{ToLogic}}", nameSynonymous={"toBoolean"}, description="{{FunctionConvertToLogic}}", params={"{{Content}}"})
  protected final Var toBoolean(Var var) throws Exception {
    return new Var(Functions.stringToBoolean(var.getObjectAsString()));
  }
  
  @CronapiMetaData(type="function", name="{{Base64ToText}}", nameSynonymous={"base64ToString"}, description="{{FunctionConvertBase64ToText}}", params={"{{ContentInBase64}}"})
  protected static final Var base64ToString(Var base64Var) throws Exception {
    Var value = new Var();
    String base64 = base64Var.getObjectAsString();
    if (base64.length() > 0) {
      value = new Var(Base64.getDecoder().decode(base64));
    }
    return value;
  }
  
  @CronapiMetaData(type="function", name="{{TextToTextBinary}}", nameSynonymous={"asciiToBinary"}, description="{{FunctionToConvertTextInTextBinary}}", params={"{{ContentInAscii}}"})
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
  
  @CronapiMetaData(type="function", name="{{TextBinaryToText}}", nameSynonymous={"binaryToAscii"}, description="{{FunctionToConvertTextBinaryToText}}", params={"{{ContentInTextBinary}}"})
  protected static final Var binaryToAscii(Var binaryVar) throws Exception {
    StringBuilder ascii = new StringBuilder();
    String binary = binaryVar.getObjectAsString();
    if (binary.length() > 0) {
      if ((binary.length()) % 8 != 0) {
        throw new Exception(Messages.getString("ParameterNotBinary"));
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
  
  @CronapiMetaData(type="function", name="{{ConvertToBytes}}", nameSynonymous={"toBytes"}, description="{{FunctionToConvertTextBinaryToText}}", params={"{{ContentInTextBinary}}"})
  protected final Var toBytes(Var var) throws Exception {
    return new Var(var.getObjectAsString().getBytes());
  }

}
