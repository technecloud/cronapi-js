package cronapi.conversion;

import java.util.Base64;

import cronapi.CronapiMetaData;


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

  /**
   * Conversão	Base 64 para Texto 
   */
  @CronapiMetaData(type="function", name="Base64 para texto", nameSynonymous={"base64ToString"}, description="Função para converter base64 para texto", params={"Conteúdo em base64"})
  protected static final String base64ToString(byte[] base64) throws Exception {
    String value = null;
    if (base64 != null && base64.length > 0) {
      byte[] result = Base64.getDecoder().decode(base64);
      value = new String(result);
    }
    return value; 
  }	
  
  /**
   * Texto Para Texto Binário 
   */
  @CronapiMetaData(type="function", name="Texto para texto binário ", nameSynonymous={"asciiToBinary"}, description="Função para converter texto para texto binário", params={"Conteúdo em ascii"})
  protected static final String asciiToBinary(String ascii) throws Exception {
    StringBuilder binary = new StringBuilder();
    if (ascii.length() > 0) {
      for (int i = 0; i < ascii.length(); i++) {
        int value = ascii.charAt(i);
        for (int j = 7; j >= 0; j--) {
          binary.append((value >> j) & 1);
        }
      }
    }
    return binary.toString();
  }

}
