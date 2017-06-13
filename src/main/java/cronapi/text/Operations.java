package cronapi.text;

import cronapi.Var;

/**
 * Classe que representa ...
 * 
 * @author Usuário de Teste
 * @version 1.0
 * @since 2017-04-03
 *
 */
public class Operations {
  
  public static final Var newText(Var text) throws Exception {
    return new Var(text);
  }
  
  public static final Var newText(Var ... text) throws Exception {
    Var result = new Var("");
    for(Var t : text) {
      result.append(t.getObjectAsString());
    }
    return result;
  }
  
  public static final Var concat(Var item, Var ... itens) throws Exception {
    for(Var t : itens) {
      item.append(t.getObjectAsString());
    }
    return item;
  }
  
  public static final Var titleCase(Var text) {
    StringBuilder titleCase = new StringBuilder();
    boolean nextTitleCase = true;
    String input = text.getObjectAsString();
    
    for(char c : input.toCharArray()) {
      if(Character.isSpaceChar(c)) {
        nextTitleCase = true;
      }
      else if(nextTitleCase) {
        c = Character.toTitleCase(c);
        nextTitleCase = false;
      }
      titleCase.append(c);
    }
    return Var.valueOf(titleCase.toString());
  }
  
}
