package cronapi.xml;

import java.io.File;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import cronapi.CronapiMetaData;
import cronapi.Var;

/**
 * Classe que representa ...
 * 
 * @author Rodrigo Reis
 * @version 1.0
 * @since 2017-03-29
 *
 */

public class Operations {

	/**
	 * Construtor
	 **/
	@CronapiMetaData(category = "XML", categorySynonymous = { "Convert", "Conversão" })
	public Operations() {
	}

	@CronapiMetaData(type = "function", name = "{{XMLGetElementValueName}}", categorySynonymous = "XMLGetElementValue", description = "{{XMLGetElementValueDescription}}", params = {
			"{{XMLGetElementValueParam0}}" })
	public static final Var XMLGetElementValue(Var element) throws Exception {
		Element elementCasted = (Element) element.getObject();
		return new Var(elementCasted.getValue());
	}

	@CronapiMetaData(type = "function", name = "{{XMLGetChildElementName}}", categorySynonymous = "XMLGetChildElement", description = "{{XMLGetChildElementDescription}}", params = {
			"{{XMLGetChildElementParam0}}","{{XMLGetChildElementParam1}}" })
	public static final Var XMLGetChildElement(Var element, Var child) throws Exception {
		Element elementCasted = (Element) element.getObject();
		Element childCasted = (Element) child.getObject();
		return new Var(elementCasted.getChild(childCasted.getName(), elementCasted.getNamespace()));

	}

	@CronapiMetaData(type = "function", name = "{{XMLGetRootName}}", categorySynonymous = "XMLGetRoot", description = "{{XMLGetRootDescription}}", params = {
			"{{XMLGetRootParam0}}" })
	public static final Var XMLGetRoot(Var element) throws Exception {
		Element elementCasted = (Element) element.getObject();
		return new Var(elementCasted.getDocument().getRootElement());
	}

	@CronapiMetaData(type = "function", name = "{{XMLGetAttributeName}}", categorySynonymous = "XMLGetAttribute", description = "{{XMLGetAttributeDescription}}", params = {
			"{{XMLGetAttributeParam0}}", "{{XMLGetAttributeParam1}}" })
	public static final Var XMLGetAttribute(Var element, Var attribute) throws Exception {
		Element elementCasted = (Element) element.getObject();
		Attribute attributeCasted = (Attribute) attribute.getObject();
		return new Var(elementCasted.getAttributeValue(attributeCasted.getName()));
	}

	@CronapiMetaData(type = "function", name = "{{XMLOpenName}}", categorySynonymous = "XMLOpen", description = "{{XMLOpenDescription}}", params = {
			"{{XMLOpenParam0}}" })
	public static final Var XMLOpen(Var xml) throws Exception {
		try {
			SAXBuilder builder = new SAXBuilder();
			return new Var(builder.build(new java.io.ByteArrayInputStream(xml.getObjectAsString().getBytes())));
		} catch (Exception e) {
			System.out.println("Erro na abertura do XML" + xml + " " + e.toString());
			throw e;
		}
	}

	@CronapiMetaData(type = "function", name = "{{XMLGetChildrenElementName}}", categorySynonymous = "XMLGetChildrenElement", description = "{{XMLGetChildrenElementDescription}}", params = {
			"{{XMLGetChildrenElementParam0}}", "{{XMLGetChildrenElementParam1}}" })
	public static final Var XMLGetChildrenElement(Var element, Var node) throws Exception {
		Element elementCasted = (Element) element.getObject();
		Element nodeCasted = (Element) element.getObject();

		if (nodeCasted == null || nodeCasted.getTextTrim().equals("")) {
			return new Var(elementCasted.getChildren());
		}
		return new Var(elementCasted.getChildren(nodeCasted.getName()));
	}

	@CronapiMetaData(type = "function", name = "{{XMLGetParentElementName}}", categorySynonymous = "XMLGetParentElement", description = "{{XMLGetParentElementDescription}}", params = {
			"{{XMLGetParentElementParam0}}" })
	public static final Var XMLGetParentElement(Var element) throws Exception {
		Element elementCasted = (Element) element.getObject();
		return new Var(elementCasted.getParentElement());
	}

	@CronapiMetaData(type = "function", name = "{{XMLGetElementTagNameName}}", categorySynonymous = "XMLGetElementTagName", description = "{{XMLGetElementTagNameDescription}}", params = {
			"{{XMLGetElementTagNameParam0}}" })
	public static final Var XMLGetElementTagName(Var element) throws Exception {
		Element elementCasted = (Element) element.getObject();
		return new Var(elementCasted.getName());
	}

	//PAREI AQUI
	//Alterar o valor de um Elemento XML
	@CronapiMetaData(type = "function", name = "{{XMLSetElementValueName}}", categorySynonymous = "XMLSetElementValue", description = "{{XMLSetElementValueDescription}}", params = {
			"{{XMLSetElementValueParam0}}", "{{XMLSetElementValueParam1}}" })
	public static final void XMLSetElementValue(Var element, Var value) throws Exception {
		Element elementCasted = (Element) element.getObject();
		elementCasted.setText(value.getObjectAsString());
	}

	// Alterar o valor de um Atributo XML
	@CronapiMetaData(type = "function", name = "{{XMLSetElementAttributeValueName}}", categorySynonymous = "XMLSetElementAttributeValue", description = "{{XMLSetElementValueDescription}}", params = {
			"{{XMLSetElementAttributeValueParam0}}", "{{XMLSetElementAttributeValueParam1}}" })
	public static final void XMLSetElementAttributeValue(Var attribute, Var value) throws Exception {
		Attribute attributeCasted = (Attribute) attribute.getObject();
		attributeCasted.setValue(value.getObjectAsString());
	}

	// 	Elemento para XML
	@CronapiMetaData(type = "function", name = "{{XMLGetElementAsXMLName}}", categorySynonymous = "XMLGetElementAsXMLName", description = "{{XMLGetElementAsXMLNameDescription}}", params = {
			"{{XMLGetElementAsXMLParam0}}" })
	public static final Var XMLGetElementAsXML(Var element) throws Exception {
		Element elementCasted = (Element) element.getObject();
		XMLOutputter xmlOut = new XMLOutputter();
		return new Var(xmlOut.outputString(elementCasted));
	}

	// 	Abrir XML de um arquivo
	@CronapiMetaData(type = "function", name = "{{XMLOpenFromFileName}}", categorySynonymous = "XMLOpenFromFile", description = "{{XMLOpenFromFileDescription}}", params = {
			"{{XMLOpenFromFileParam0}}" })
	public static final Var XMLOpenFromFile(Var file) throws Exception {
		File fileCasted = (File) file.getObject();
		SAXBuilder builder = new SAXBuilder();
		return new Var(builder.build(fileCasted));
	}

	// 	Obter Nós	
	@CronapiMetaData(type = "function", name = "{{XpathCompileName}}", categorySynonymous = "XpathCompile", description = "{{XpathCompileDescription}}", params = {
			"{{XpathCompileParam0}}", "{{XpathCompileParam1}}" })
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
