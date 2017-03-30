package cronapi.xml;

import java.io.File;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import cronapi.Var;
import cronapi.CronapiMetaData;

/**
 * Classe que representa ...
 * 
 * @author Usuário de Teste
 * @version 1.0
 * @since 2017-03-29
 *
 */

public class Operations {

	/**
	 * Construtor
	 **/
	@CronapiMetaData(category = "XML", categorySynonymous = { "XML", "Operations" })
	public Operations() {
	}

	@CronapiMetaData(type = "function", name = "Obtém valor do elemento", categorySynonymous = "XMLGetElementValue", description = "Função que retorna o valor de um elemento", params = {
			"Elemento passado para obter-se o valor" })
	public static final Var XMLGetElementValue(Var element) throws Exception {
		Element elementCasted = (Element) element.getObject();
		return new Var(elementCasted.getValue());
	}

	@CronapiMetaData(type = "function", name = "Obtém o primeiro filho do elemento", categorySynonymous = "XMLGetChildElement", description = "Função para retornar o nó", params = {
			"Elemento passado para obter-se o valor", "Filho a ser obtido do elemento" })
	public static final Var XMLGetChildElement(Var element, Var child) throws Exception {
		Element elementCasted = (Element) element.getObject();
		Element childCasted = (Element) child.getObject();
		return new Var(elementCasted.getChild(childCasted.getName(), elementCasted.getNamespace()));

	}

	@CronapiMetaData(type = "function", name = "Obtém a raiz do elemento", categorySynonymous = "XMLGetRoot", description = "Função que retorna o elemento raiz a partir de um elemento", params = {
			"Elemento passado para obter-se a raiz" })
	public static final Var XMLGetRoot(Var element) throws Exception {
		Element elementCasted = (Element) element.getObject();
		return new Var(elementCasted.getDocument().getRootElement());
	}

	@CronapiMetaData(type = "function", name = "Obtém o atributo do elemento", categorySynonymous = "XMLGetAttribute", description = "Função que retorna o elemento raiz a partir de um elemento", params = {
			"Elemento passado para obter-se a raiz", "Atributo a ser obtido" })
	public static final Var XMLGetAttribute(Var element, Var attribute) throws Exception {
		Element elementCasted = (Element) element.getObject();
		Attribute attributeCasted = (Attribute) attribute.getObject();
		return new Var(elementCasted.getAttributeValue(attributeCasted.getName()));
	}

	@CronapiMetaData(type = "function", name = "Cria Document", categorySynonymous = "XMLOpen", description = "Função que cria um objeto Document a partir de uma String", params = {
			"Elemento passado para obter-se a raiz" })
	public static final Var XMLOpen(Var xml) throws Exception {
		try {
			SAXBuilder builder = new SAXBuilder();
			return new Var(builder.build(new java.io.ByteArrayInputStream(xml.getObjectAsString().getBytes())));
		} catch (Exception e) {
			System.out.println("Erro na abertura do XML" + xml + " " + e.toString());
			throw e;
		}
	}

	@CronapiMetaData(type = "function", name = "Busca filhos do elemento", categorySynonymous = "XMLGetChildrenElement", description = "Função que retorna os filhos do tipo de um determinado elemento", params = {
			"Elemento passado para buscar os filhos", "Elemento do tipo a ser buscado" })
	public static final Var XMLGetChildrenElement(Var element, Var node) throws Exception {
		Element elementCasted = (Element) element.getObject();
		Element nodeCasted = (Element) element.getObject();

		if (nodeCasted == null || nodeCasted.getTextTrim().equals("")) {
			return new Var(elementCasted.getChildren());
		}
		return new Var(elementCasted.getChildren(nodeCasted.getName()));
	}

	@CronapiMetaData(type = "function", name = "Retorna o elemento pai", categorySynonymous = "XMLGetParentElement", description = "Função que retorna o pai de um elemento", params = {
			"Elemento a ser buscado o pai" })
	public static final Var XMLGetParentElement(Var element) throws Exception {
		Element elementCasted = (Element) element.getObject();
		return new Var(elementCasted.getParentElement());
	}

	@CronapiMetaData(type = "function", name = "Retorna a tag do elemento", categorySynonymous = "XMLGetElementTagName", description = "Função que retorna o nome da tag do elemento", params = {
			"Elemento a ser buscado a tag" })
	public static final Var XMLGetElementTagName(Var element) throws Exception {
		Element elementCasted = (Element) element.getObject();
		return new Var(elementCasted.getName());
	}

	//PAREI AQUI
	//Alterar o valor de um Elemento XML
	@CronapiMetaData(type = "function", name = "Altera o texto do elemento", categorySynonymous = "XMLSetElementValue", description = "Função que altera o texto do elemento", params = {
			"Elemento a ser alterado", "texto a ser inserido" })
	public static final void XMLSetElementValue(Var element, Var value) throws Exception {
		Element elementCasted = (Element) element.getObject();
		elementCasted.setText(value.getObjectAsString());
	}

	// Alterar o valor de um Atributo XML
	@CronapiMetaData(type = "function", name = "Altera o valor do atributo", categorySynonymous = "XMLSetElementAttributeValue", description = "Função que altera o valor do atributo", params = {
			"Atributo a ser alterado", "valor a ser inserido" })
	public static final void XMLSetElementAttributeValue(Var attribute, Var value) throws Exception {
		Attribute attributeCasted = (Attribute) attribute.getObject();
		attributeCasted.setValue(value.getObjectAsString());
	}

	// 	Elemento para XML
	@CronapiMetaData(type = "function", name = "Retorna o elemento como xml", categorySynonymous = "XMLSetElementAttributeValue", description = "Função que retorna o elemento como String com estrutura de xml", params = {
			"Elemento a ser retornado" })
	public static final Var XMLGetElementAsXML(Var element) throws Exception {
		Element elementCasted = (Element) element.getObject();
		XMLOutputter xmlOut = new XMLOutputter();
		return new Var(xmlOut.outputString(elementCasted));
	}

	// 	Abrir XML de um arquivo
	@CronapiMetaData(type = "function", name = "Instância o Document de um File", categorySynonymous = "XMLOpenFromFile", description = "Função que cria um objeto Document a partir de um objeto File", params = {
			"Arquivo base para a criação do Document" })
	public static final Var XMLOpenFromFile(Var file) throws Exception {
		File fileCasted = (File) file.getObject();
		SAXBuilder builder = new SAXBuilder();
		return new Var(builder.build(fileCasted));
	}

	// 	Obter Nós	
	@CronapiMetaData(type = "function", name = "Retorna os nós do elemento", categorySynonymous = "XpathCompile", description = "Função que cria itera o Element e retorna os elementos que são igual a path", params = {
			"Elemento a ser iterado", "Elemento chave da busca" })
	public final static Var XpathCompile(Var xml, Var path) {
		java.util.ArrayList<Element> elements = new java.util.ArrayList<Element>();
		Element xmlCasted = (Element) xml.getObject();
		Element pathCasted = (Element) path.getObject();
		for (Element el : xmlCasted.getChildren()) {
			if (el.getName().equals(pathCasted.getName()))
				elements.add(el);
		}
		return new Var(elements);
	}

}
