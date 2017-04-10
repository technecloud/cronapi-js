package cronapi.email;


/**
 * Classe que representa ...
 * 
 * @author Usuário de Teste
 * @version 1.0
 * @since 2017-04-10
 *
 */
 
public class Recipient {

	/**
	 * Construtor
	 **/
	public Recipient (String to, Type type){
	  this.to = to;
	  this.type = type;
	}
	
	public String to;
	public Type type;
	
	public enum Type {
		TO, CC, BCC
	}

}
