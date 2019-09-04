package br.com.cronapi.xml;

import com.google.gson.JsonObject;
import cronapi.Var;
import cronapi.xml.Operations;
import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.InputStream;

import static cronapi.xml.Operations.xmlFromStrng;
import static cronapi.xml.Operations.xmltoJson;

public class OperationsTest {

    private Var booksXml;

    @BeforeClass
    private void before() throws Exception {
        try (InputStream booksInput = getClass().getResourceAsStream("/books.xml")) {
            String xml = IOUtils.toString(booksInput);
            booksXml = Var.valueOf(xml);
        }
    }

    @AfterClass
    private void after() {
        booksXml = null;
    }

    private Var getXMLException() {
        return Var.valueOf("<purcaseOrder PurchaseOrderNumber=\\\"99\\\"></purcaseOrder>");
    }

    @Test
    public void testXmltoJsonException() throws Exception {
        Assert.assertThrows(JSONException.class, () -> xmltoJson(getXMLException()));
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
    void newXMLEmpty() throws Exception {
        Var newXMLEmpty = Operations.newXMLEmpty();
        Assert.assertTrue(newXMLEmpty.getObject() instanceof Document);
        Document document = (Document)newXMLEmpty.getObject();
        Assert.assertFalse(document.hasRootElement());
    }

//    @Test
//    void testNewXMLEmpty() {
//    }
//
//    @Test
//    void XMLOpenFromFile() {
//    }
//
//    @Test
//    void XMLOpen() {
//    }
//
//    @Test
//    void XMLcreateElement() {
//    }
//
//    @Test
//    void XMLaddElement() {
//    }
//
//    @Test
//    void hasRootElement() {
//    }
//
//    @Test
//    void getRootElement() {
//    }
//
//    @Test
//    void XMLDocumentToString() {
//    }
//
//    @Test
//    void XMLElementToString() {
//    }
//
//    @Test
//    void XMLGetChildElement() {
//    }
//
//    @Test
//    void XMLSetElementAttributeValue() {
//    }
//
//    @Test
//    void XMLGetAttributeValue() {
//    }
//
//    @Test
//    void XMLGetParentElement() {
//    }
//
//    @Test
//    void XMLSetElementValue() {
//    }
//
//    @Test
//    void XMLGetElementValue() {
//    }
//
//    @Test
//    void XMLRemoveElement() {
//    }
//
//    @Test
//    void XMLGetElementTagName() {
//    }
//
//    @Test
//    void XMLChangeNodeName() {
//    }

}
