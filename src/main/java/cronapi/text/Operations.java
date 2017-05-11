package cronapi.text;

import cronapi.CronapiMetaData;
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

	public static final Var newText(Var... text) throws Exception {
		Var result = new Var("");
		for (Var t : text) {
			result.append(t.getObjectAsString());
		}
		return result;
	}
	
	
		public static final Var concat (Var item, Var... itens) throws Exception {
		for (Var t : itens) {
			item.append(t.getObjectAsString());
		}
		return item;
	}
	
	
	
	
	
		public static void main(String[] args) {
		Var v = new Var("Começo");
		Var v2 = new Var(" meio e fim");
		try {
			System.out.println("teste:" + Operations.newText(v, v2,v,v,v2));
			
		} catch (Exception e) {

		}
	}
	

}
