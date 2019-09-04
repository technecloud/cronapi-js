package br.com.cronapi.json;

import com.jayway.jsonpath.JsonPath;
import cronapi.Var;
import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.InputStream;

import static cronapi.json.Operations.GSON_CONFIGURATION;
import static cronapi.json.Operations.toJson;
import static cronapi.json.Operations.toXml;

public class OperationsTest {

    private Var booksJson;

    @BeforeMethod
    public void setUp() throws Exception {
        try (InputStream booksInput = getClass().getResourceAsStream("/books.json")) {
            booksJson = toJson(Var.valueOf(IOUtils.toString(booksInput)));
        }
    }

    @AfterMethod
    public void tearDown() {
    }

    @Test
    public void testCreateObjectJson() {
    }

    @Test
    public void testDeleteObjectFromJson() {
    }

    @Test
    public void testGetJsonOrMapField() {
    }

    @Test
    public void testSetJsonOrMapField() {
    }

    @Test
    public void testToJson() {
    }

    @Test
    public void testToList() {
    }

    @Test
    public void testToMap() {
    }

    @Test
    public void testToXml() throws Exception {
        Assert.assertTrue(toXml(booksJson).getObject() instanceof Document);
    }
}