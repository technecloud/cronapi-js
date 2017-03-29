package cronapi.xml;

import java.io.File;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

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
	public Operations() {
	}

	@CronapiMetaData(type = "function", name = "Obtém valor do elemento", categorySynonymous = "XMLGetElementValue", description = "Função que retorna o valor de um elemento", params = {
			"Elemento passado para obter-se o valor" })
	public static final String XMLGetElementValue(Element element) throws Exception {
		return element.getValue();
	}

	// 	Obter Filho de Elemento
	// function XMLGetChildElement(node, childName) {
	// var c = node.getElementsByTagName(childName);
	// if (c.length > 0) 
	// return c[0]; 
	//	}
	@CronapiMetaData(type = "function", name = "Obtém o primeiro filho do elemento", categorySynonymous = "XMLGetChildElement", description = "Função para retornar o nó", params = {
			"Elemento passado para obter-se o valor", "Filho a ser obtido do elemento" })
	public static final Element XMLGetChildElement(Element element, Element child) throws Exception {
		return element.getChild(child.getName(), element.getNamespace());

	}

	// 	Obter Raiz
	// 	    return rootVar;
	//   }	function XMLGetRoot(doc) {
	//   if (doc) return doc.documentElement;
	// }
	@CronapiMetaData(type = "function", name = "Obtém a raiz do elemento", categorySynonymous = "XMLGetRoot", description = "Função que retorna o elemento raiz a partir de um elemento", params = {
			"Elemento passado para obter-se a raiz" })
	public static final Element XMLGetRoot(Element element) throws Exception {
		return element.getDocument().getRootElement();
	}

	// Obter Atributo
	// 	function XMLGetAttribute(node, attribute) {
	//   return node.getAttribute(attribute);
	// }
	@CronapiMetaData(type = "function", name = "Obtém o atributo do elemento", categorySynonymous = "XMLGetAttribute", description = "Função que retorna o elemento raiz a partir de um elemento", params = {
			"Elemento passado para obter-se a raiz" })
	public static final String XMLGetAttribute(Element element, Attribute attribute) throws Exception {
		return element.getAttributeValue(attribute.getName());
	}

	// 	Abrir XML
	// 	function XMLOpen(XMLText) {
	//   var doc = null;
	//   if (document.implementation && document.implementation.createDocument) {//Mozzila
	//   var domParser = new DOMParser();
	//   doc = domParser.parseFromString(XMLText, 'application/xml');
	//   fixXMLDocument(doc);
	//   return doc;
	//   }
	//   else {//IE
	//     doc = new ActiveXObject("MSXML2.DOMDocument");
	//     doc.loadXML(XMLText);
	//   }
	//   return doc;
	// 	};
	@CronapiMetaData(type = "function", name = "Cria Document", categorySynonymous = "XMLOpen", description = "Função que cria um objeto Document a partir de uma String", params = {
			"Elemento passado para obter-se a raiz" })
	public static final Document XMLOpen(String xml) throws Exception {
		try {
			SAXBuilder builder = new SAXBuilder();
			return builder.build(new java.io.ByteArrayInputStream(xml.getBytes()));
		} catch (Exception e) {
			System.out.println("Erro na abertura do XML" + xml + " " + e.toString());
			throw e;
		}
	}

	// 	Filhos de um Elemento XML
	// 	function XMLGetChildrenElement(node, childName) {
	//   if (childName) {
	//     return node.getElementsByTagName(childName);
	//   }
	//   else {
	//     return node.childNodes;  
	//   }
	// }
	@CronapiMetaData(type = "function", name = "Busca filhos do elemento", categorySynonymous = "XMLGetChildrenElement", description = "Função que retorna os filhos do tipo de um determinado elemento", params = {
			"Elemento passado para buscar os filhos", "Elemento do tipo a ser buscado" })
	public static final List<Element> XMLGetChildrenElement(Element element, Element node) throws Exception {
		if (node == null || node.getTextTrim().equals("")) {
			return element.getChildren();
		}
		return element.getChildren(node.getName());
	}

	// 	Obter o Pai de um Elemento XML
	// function XMLGetParentElement(node) {
	//   return node.parentNode
	@CronapiMetaData(type = "function", name = "Retorna o elemento pai", categorySynonymous = "XMLGetParentElement", description = "Função que retorna o pai de um elemento", params = {
			"Elemento a ser buscado o pai" })
	public static final Element XMLGetParentElement(Element element) throws Exception {
		return element.getParentElement();
	}

	// 	Obter o Nome de um Elemento XML
	// function ebfXMLGetElementTagName(node) {
	//   return node.tagName;
	// }
	@CronapiMetaData(type = "function", name = "Retorna a tag do elemento", categorySynonymous = "XMLGetElementTagName", description = "Função que retorna o nome da tag do elemento", params = {
			"Elemento a ser buscado a tag" })
	public static final String XMLGetElementTagName(Element element) throws Exception {
		return element.getName();
	}

	//Alterar o valor de um Elemento XML
	@CronapiMetaData(type = "function", name = "Altera o texto do elemento", categorySynonymous = "XMLSetElementValue", description = "Função que altera o texto do elemento", params = {
			"Elemento a ser alterado", "texto a ser inserido" })
	public static final void XMLSetElementValue(Element element, String value) throws Exception {
		element.setText(value);
	}

	// Alterar o valor de um Atributo XML
	@CronapiMetaData(type = "function", name = "Altera o valor do atributo", categorySynonymous = "XMLSetElementAttributeValue", description = "Função que altera o valor do atributo", params = {
			"Atributo a ser alterado", "valor a ser inserido" })
	public static final void XMLSetElementAttributeValue(Attribute attribute, String value) throws Exception {
		attribute.setValue(value);
	}

	// 	Elemento para XML
	@CronapiMetaData(type = "function", name = "Retorna o elemento como xml", categorySynonymous = "XMLSetElementAttributeValue", description = "Função que retorna o elemento como String com estrutura de xml", params = {
			"Elemento a ser retornado" })
	public static final String XMLGetElementAsXML(Element element) throws Exception {
		XMLOutputter xmlOut = new XMLOutputter();
		return xmlOut.outputString(element);
	}

	// 	Abrir XML de um arquivo
	@CronapiMetaData(type = "function", name = "Instância o Document de um File", categorySynonymous = "XMLOpenFromFile", description = "Função que cria um objeto Document a partir de um objeto File", params = {
			"Arquivo base para a criação do Document" })
	public static final Document XMLOpenFromFile(File file) throws Exception {
		SAXBuilder builder = new SAXBuilder();
		return builder.build(file);
	}

	// 	Obter Nós	
	@CronapiMetaData(type = "function", name = "Retorna os nós do elemento", categorySynonymous = "XpathCompile", description = "Função que cria itera o Element e retorna os elementos que são igual a path", params = {
			"Elemento a ser iterado", "Elemento chave da busca" })
	public final static List<Element> XpathCompile(Element xml, Element path) {
		java.util.ArrayList<Element> elements = new java.util.ArrayList<Element>();
		for (Element el : xml.getChildren()) {
			if (el.getName().equals(path.getName()))
				elements.add(el);
		}
		return elements;
	}

}
