package br.com.cronapi.xml;

import com.google.gson.JsonObject;
import cronapi.Var;
import cronapi.xml.Operations;
import org.apache.commons.io.IOUtils;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.json.JSONException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static cronapi.xml.Operations.*;

public class XmlTest {

    private Var booksXml;

    @Before
    public void before() throws Exception {
        try (InputStream booksInput = getClass().getResourceAsStream("/books.xml")) {
            String xml = IOUtils.toString(booksInput);
            booksXml = Var.valueOf(xml);
        }
    }

    @After
    public void after() {
        booksXml = null;
    }

    private Var getXMLException() {
        return Var.valueOf("<purcaseOrder PurchaseOrderNumber=\\\"99\\\"></purcaseOrder>");
    }

    private String getPath(String s) {
        return getClass().getResource(s).getPath();
    }

    @Test(expected = JSONException.class)
    public void testXmltoJsonException() throws Exception {
        xmltoJson(getXMLException());
    }

    @Test
    public void testXMLtoJson() throws Exception {
        Var item = xmltoJson(booksXml);
        Assert.assertTrue(item.getObject() instanceof JsonObject);
    }

    @Test
    public void testxmlFromString() throws Exception {
        Assert.assertTrue(xmlFromStrng(booksXml).getObject() instanceof Document);
    }

    @Test
    public void newXMLEmpty() throws Exception {
        Var newXMLEmpty = Operations.newXMLEmpty();
        Assert.assertTrue(newXMLEmpty.getObject() instanceof Document);
        Document document = (Document) newXMLEmpty.getObject();
        Assert.assertFalse(document.hasRootElement());
    }

    @Test
    public void testNewXMLEmpty() throws Exception {
        Var xmlRoot = Var.valueOf(new Element("teste"));
        Var retorno = Operations.newXMLEmpty(xmlRoot);
        Document document = (Document) retorno.getObject();
        Assert.assertTrue(document.hasRootElement());
        Assert.assertEquals(((Element) document.getContent().get(0)).getName(), "teste");
    }

    @Test
    public void XMLOpenFromFile() throws Exception {
        String absPath = getPath("/books.xml");
        Var xmlRetorno = Operations.XMLOpenFromFile(Var.valueOf(absPath));
        Assert.assertTrue(xmlRetorno.getObject() instanceof Document);
    }

    @Test
    public void XMLOpen() throws Exception {
        Var xmlRetorno = Operations.XMLOpen(Var.valueOf("https://www.w3schools.com/xml/plant_catalog.xml"));
        Assert.assertTrue(xmlRetorno.getObject() instanceof Document);
    }

    @Test
    public void XMLOpenNull() throws Exception {
        Var xmlRetorno = Operations.XMLOpen(Var.valueOf(""));
        Assert.assertNull(xmlRetorno.getObject());
    }

    @Test
    public void XMLcreateElement() {
        Var xmlRetorno = Operations.XMLcreateElement(Var.valueOf("name"), Var.valueOf("value"));
        Assert.assertTrue(xmlRetorno.getObject() instanceof Element);
    }

    @Test
    public void XMLcreateElementNull() {
        Var xmlRetorno = Operations.XMLcreateElement(Var.VAR_NULL, Var.valueOf("value"));
        Assert.assertNull(xmlRetorno.getObject());
    }

    @Test
    public void XMLaddElementParent() {
        Var xmlElementParent = Operations.XMLcreateElement(Var.valueOf("parante1"), Var.valueOf("value"));
        Var xmlElement = Operations.XMLcreateElement(Var.valueOf("element2"), Var.valueOf("value"));
        Operations.XMLaddElement(xmlElementParent, xmlElement);
    }

    @Test
    public void XMLaddElementDocumentHasRoot() throws Exception {
        Var xmlRetorno = Operations.XMLOpen(Var.valueOf("https://www.w3schools.com/xml/plant_catalog.xml"));
        Var xmlElement = Operations.XMLcreateElement(Var.valueOf("element2"), Var.valueOf("value"));
        Operations.XMLaddElement(xmlRetorno, xmlElement);
    }

    @Test
    public void XMLaddElementDocumentNotHasRoot() throws Exception {
        Var xmlRetorno = Operations.newXMLEmpty();
        Var xmlElement = Operations.XMLcreateElement(Var.valueOf("element2"), Var.valueOf("value"));
        Operations.XMLaddElement(xmlRetorno, xmlElement);
    }

    @Test
    public void hasRootElementFalse() {
        Var xmlString = Var.valueOf("");
        Var xmlRetorno = Operations.hasRootElement(xmlString);
        Assert.assertFalse(xmlRetorno.getObjectAsBoolean());
    }

    @Test
    public void hasRootElementDocumentFalse() throws Exception {
        Var xml = Operations.newXMLEmpty();
        Var xmlRetorno = Operations.hasRootElement(xml);
        Assert.assertFalse(xmlRetorno.getObjectAsBoolean());
    }

    @Test
    public void hasRootElementElementFalse() throws Exception {
        Var xml = Operations.XMLcreateElement(Var.valueOf("name"), Var.valueOf("value"));
        Var xmlRetorno = Operations.hasRootElement(xml);
        Assert.assertFalse(xmlRetorno.getObjectAsBoolean());
        String absPath = getPath("/hasRootObjects.xml");
        Var xmlRetornoUrl = Operations.XMLOpenFromFile(Var.valueOf(absPath));
        xmlRetorno = Operations.hasRootElement(Var.valueOf(((Document)xmlRetornoUrl.getObject()).getContent(0)));
        Assert.assertTrue(xmlRetorno.getObjectAsBoolean());
    }

    @Test
    public void getRootElement() throws Exception {
        Assert.assertNull(Operations.getRootElement(Var.valueOf("teste")).getObject());
        String absPath = getPath("/books.xml");
        Var xmlRetorno = Operations.XMLOpenFromFile(Var.valueOf(absPath));
        Assert.assertTrue(xmlRetorno.getObject() instanceof Document);
        Var root = Operations.getRootElement(xmlRetorno);
        Assert.assertTrue(root.getObject() instanceof Element);
        absPath = getPath("/hasRootObjects.xml");
        Var xmlRetornoUrl = Operations.XMLOpenFromFile(Var.valueOf(absPath));
        xmlRetorno = Operations.getRootElement(Var.valueOf(((Document)xmlRetornoUrl.getObject()).getContent(0)));
        Assert.assertEquals(((Element)xmlRetorno.getObject()).getName(), "Identification_List");
        Var xml = Operations.newXMLEmpty();
        xmlRetorno = Operations.getRootElement(xml);
        Assert.assertFalse(xmlRetorno.getObjectAsBoolean());
    }

    @Test
    public void XMLDocumentToString() throws Exception {
        String absPath = getPath("/books.xml");
        Var xmlRetorno = Operations.XMLOpenFromFile(Var.valueOf(absPath));
        Assert.assertTrue(xmlRetorno.getObject() instanceof Document);
        xmlRetorno = Operations.XMLDocumentToString(xmlRetorno);
        Assert.assertTrue(xmlRetorno.getObject() instanceof String);
    }

    @Test
    public void XMLElementToString() {
        Var xml = Operations.XMLcreateElement(Var.valueOf("name"), Var.valueOf("value"));
        Assert.assertTrue(Operations.XMLElementToString(xml).getObject() instanceof String);
        Assert.assertNull(Operations.XMLElementToString(Var.valueOf(Var.VAR_NULL)).getObject());
    }

    @Test
    public void XMLGetChildElement() throws Exception {
        Assert.assertNull(Operations.XMLGetChildElement(Var.valueOf(Var.VAR_NULL), Var.valueOf(Var.VAR_NULL)).getObject());
        Assert.assertNull(Operations.XMLGetChildElement(Var.valueOf("Var.VAR_NULL"), Var.valueOf(Var.VAR_NULL)).getObject());
        String absPath = getPath("/books.xml");
        Var xmlRetorno = Operations.XMLOpenFromFile(Var.valueOf(absPath));
        Assert.assertNull(Operations.XMLGetChildElement(xmlRetorno, Var.valueOf(Var.VAR_NULL)).getObject());
        Var retorno = Operations.XMLGetChildElement(Var.valueOf(((Document)xmlRetorno.getObject()).getRootElement().getChildren("store").get(0)), Var.valueOf("name"));
        Assert.assertEquals(retorno.getObjectAsList().size(),1);
        retorno = Operations.XMLGetChildElement(Var.valueOf(((Document)xmlRetorno.getObject())), Var.valueOf("store"));
        Assert.assertEquals(retorno.getObjectAsList().size(),1);
        retorno = Operations.XMLGetChildElement(Var.valueOf(((Document)xmlRetorno.getObject())), Var.valueOf("root"));
        Assert.assertEquals(retorno.getObjectAsList().size(),1);
        retorno = Operations.XMLGetChildElement(Var.valueOf(((Document)xmlRetorno.getObject()).getRootElement().getChildren("store").get(0)), Var.valueOf(((Document)xmlRetorno.getObject()).getRootElement().getChildren("store").get(0)));
        Assert.assertEquals(retorno.getObjectAsList().size(),0);
        retorno = Operations.XMLGetChildElement(Var.valueOf(((Document)xmlRetorno.getObject()).getContent(0)), Var.VAR_NULL);
        Assert.assertEquals(retorno.getObjectAsList().size(),2);
    }

    @Test(expected = Exception.class)
    public void XMLSetElementAttributeValueNull() throws Exception {
        Operations.XMLSetElementAttributeValue(Var.valueOf(Var.VAR_NULL), Var.valueOf(Var.VAR_NULL), Var.valueOf(Var.VAR_NULL));
    }

    @Test(expected = Exception.class)
    public void XMLSetElementAttributeValueNotElement() throws Exception {
        Operations.XMLSetElementAttributeValue(Var.valueOf("teste"), Var.valueOf("teste"), Var.valueOf("teste"));
    }

    @Test()
    public void XMLSetElementAttributeValue() throws Exception {
        Var xml = Operations.XMLcreateElement(Var.valueOf("name"), Var.valueOf("value"));
        Operations.XMLSetElementAttributeValue(xml, Var.valueOf("teste"), Var.valueOf("teste"));
    }

    @Test
    public void XMLGetAttributeValue() throws Exception {
        Assert.assertNull(Operations.XMLGetAttributeValue(Var.valueOf(null), Var.valueOf(null)).getObject());
        String absPath = getPath("/books.xml");
        Var xmlRetorno = Operations.XMLOpenFromFile(Var.valueOf(absPath));
        xmlRetorno = Operations.XMLGetAttributeValue(Var.valueOf(((Document)xmlRetorno.getObject()).getRootElement()), Var.valueOf("name"));
        Assert.assertNull(xmlRetorno.getObject());
    }

    @Test
    public void XMLGetParentElementNull() throws Exception {
        Assert.assertEquals(XMLGetParentElement(Var.valueOf(null)).getObjectAsString(), "");
        Var xml = Operations.XMLcreateElement(Var.valueOf("name"), Var.valueOf("value"));
        Assert.assertEquals(XMLGetParentElement(xml).getObjectAsString(), "");
        String absPath = getPath("/books.xml");
        Var xmlRetorno = Operations.XMLOpenFromFile(Var.valueOf(absPath));
        List<Element> list = new ArrayList<>();
        list.add(((Document)xmlRetorno.getObject()).getRootElement().getChildren("store").get(0));
        Var retorno = XMLGetParentElement(Var.valueOf(list));
        Assert.assertTrue(retorno.getObject() instanceof Content);
    }

    @Test(expected = Exception.class)
    public void XMLSetElementValueNotElement() throws Exception {
        Operations.XMLSetElementValue(Var.valueOf("teste"), Var.valueOf("teste"));
    }

    @Test
    public void XMLSetElementValue() throws Exception {
        Var xml = Operations.XMLcreateElement(Var.valueOf("name"), Var.valueOf("value"));
        Operations.XMLSetElementValue(xml, Var.valueOf("teste"));
        Operations.XMLSetElementValue(xml, xml);
    }

    @Test
    public void XMLGetElementValueStringEmpty() throws Exception {
        Assert.assertEquals(Operations.XMLGetElementValue(Var.valueOf("")).getObjectAsString(), "");
    }

    @Test
    public void XMLGetElementValueElement() throws Exception {
        Var xml = Operations.XMLcreateElement(Var.valueOf("name"), Var.valueOf("value"));
        Assert.assertEquals(Operations.XMLGetElementValue(xml).getObjectAsString(), "value");
    }

    @Test
    public void XMLGetElementValue() throws Exception {
        Var xml = Operations.XMLcreateElement(Var.valueOf("name"), Var.valueOf("value"));
        List<Element> list = new ArrayList<>();
        list.add((Element)xml.getObject());
        Assert.assertEquals(Operations.XMLGetElementValue(Var.valueOf(list)).getObjectAsString(), "value");
        List<Integer> integerList = new ArrayList<>();
        integerList.add(1);
        integerList.add(2);
        Assert.assertEquals(Operations.XMLGetElementValue(Var.valueOf(integerList)).getObjectAsString(), "12");
    }

    @Test
    public void XMLRemoveElementNull() throws Exception {
        Assert.assertFalse(Operations.XMLRemoveElement(Var.valueOf(Var.VAR_NULL), Var.valueOf(Var.VAR_NULL)).getObjectAsBoolean());
    }

    @Test
    public void XMLRemoveElementWithParent() throws Exception {
        Var xml = Operations.XMLcreateElement(Var.valueOf("name"), Var.valueOf("value"));
        Assert.assertTrue(Operations.XMLRemoveElement(xml, Var.valueOf(Var.VAR_NULL)).getObjectAsBoolean());
    }

    @Test
    public void XMLRemoveElement() throws Exception {
        String absPath = getPath("/hasRootObjects.xml");
        Var xmlRetorno = Operations.XMLOpenFromFile(Var.valueOf(absPath));
        Assert.assertTrue(Operations.XMLRemoveElement(Var.valueOf(((Document)xmlRetorno.getObject()).getContent(0)), Var.valueOf("value")).getObjectAsBoolean());
        Element element = (Element)((Document)xmlRetorno.getObject()).getContent(0).getDocument().getRootElement();
        Assert.assertTrue(Operations.XMLRemoveElement(Var.valueOf(((Document)xmlRetorno.getObject()).getContent(0)), Var.valueOf(element.getChildren().get(0))).getObjectAsBoolean());
    }

    @Test
    public void XMLGetElementTagNameNull() throws Exception {
        Assert.assertNull(Operations.XMLGetElementTagName(Var.valueOf(Var.VAR_NULL)).getObject());
    }

    @Test
    public void XMLGetElementTagNameNotElement() throws Exception {
        Assert.assertNull(Operations.XMLGetElementTagName(Var.valueOf("Var.VAR_NULL")).getObject());
    }

    @Test
    public void XMLGetElementTagName() throws Exception {
        Var xml = Operations.XMLcreateElement(Var.valueOf("name"), Var.valueOf("value"));
        Assert.assertEquals(Operations.XMLGetElementTagName(xml).getObject(), "name");
    }

    @Test
    public void XMLChangeNodeNameNull() throws Exception {
        Assert.assertFalse(Operations.XMLChangeNodeName(Var.valueOf(Var.VAR_NULL), Var.valueOf(Var.VAR_NULL)).getObjectAsBoolean());
    }

    @Test
    public void XMLChangeNodeNameFalse() throws Exception {
        Assert.assertFalse(Operations.XMLChangeNodeName(Var.valueOf("root"), Var.valueOf("root")).getObjectAsBoolean());
    }

    @Test
    public void XMLChangeNodeName() throws Exception {
        Var xml = Operations.XMLcreateElement(Var.valueOf("name"), Var.valueOf("value"));
        Assert.assertTrue(Operations.XMLChangeNodeName(xml, Var.valueOf("root")).getObjectAsBoolean());
    }

}
