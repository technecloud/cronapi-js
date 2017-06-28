package cronapi.i18n;

import java.text.MessageFormat;

import cronapi.Var;

/**
 * Classe que representa ...
 * 
 * @author Usuário de Teste
 * @version 1.0
 * @since 2017-06-26
 *
 */

public class Operations {

  /**
   * Construtor
   **/
  public static final Var translate(Var keyI18n, Var... params) throws Exception {
    String text = keyI18n.getObjectAsString();
    text = MessageFormat.format(text, params);
    return Var.valueOf(text);
  }

}
