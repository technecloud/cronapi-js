package br.com.cronapi.conversion;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import cronapi.Utils;
import cronapi.Var;
import cronapi.i18n.Messages;
import org.jdom2.Document;
import org.jdom2.Element;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class Operations {

  private TimeZone last;

  @BeforeClass
  private void before() {
    last = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Messages.set(Locale.ENGLISH);
  }

  @AfterClass
  private void after() {
    TimeZone.setDefault(last);
  }

  @Test
  public void dateConversion() {
    Assert.assertTrue(cronapi.conversion.Operations.convertLongToDate(Var.valueOf("1554914251430")).getObjectAsDateTime() instanceof GregorianCalendar);
  }

  @Test
  public void stringToIntConversion() {
    Assert.assertTrue(Var.valueOf("").getObjectAsInt() == 0);
    Assert.assertTrue(Var.valueOf("").getObjectAsDouble() == 0.0);
    Assert.assertTrue(Var.valueOf("").getObjectAsLong() == 0L);
  }

  private Var getTestDate() {
    return Var.valueOf("2012-05-12T04:05:24Z");
  }

  private class AnyObject {
    private String test;

    public String getTest() {
      return test;
    }

    public void setTest(String test) {
      this.test = test;
    }
  }

  @Test
  public void getObjectAsStringConversion() {
    Assert.assertEquals(Var.valueOf("12").getObjectAsString(),  "12");
    Assert.assertEquals(Var.valueOf("value").getObjectAsString(),  "value");
    Assert.assertEquals(Var.valueOf(12).getObjectAsString(),  "12");
    Assert.assertEquals(Var.valueOf(12.14556896).getObjectAsString(),  "12.14556896");
    Assert.assertEquals(Var.valueOf(12.1).getObjectAsString(),  "12.1");
    Assert.assertEquals(Var.valueOf(0.1).getObjectAsString(),  "0.1");
    Assert.assertEquals(Var.valueOf(true).getObjectAsString(),  "true");
    Assert.assertEquals(Var.valueOf(false).getObjectAsString(),  "false");

    Date current = new Date();

    Assert.assertEquals(Var.valueOf(current).getObjectAsString(), Utils.getISODateFormat().format(current));
    Assert.assertEquals(Var.valueOf(getTestDate()).getObjectAsString(), "2012-05-12T04:05:24Z");
    Assert.assertEquals(Var.valueOf(new File("/tmp/test.txt")).getObjectAsString(),  "/tmp/test.txt");

    ByteArrayInputStream stream = new ByteArrayInputStream("test".getBytes());
    Assert.assertEquals(Var.valueOf(stream).getObjectAsString(),  "test");

    Document doc = new Document();
    Element newElement = new Element("test");
    newElement.setText("any");
    doc.addContent(newElement);

    Assert.assertEquals(Var.valueOf(doc).getObjectAsString(),  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<test>any</test>\r\n");

    Assert.assertEquals(Var.valueOf(newElement).getObjectAsString(),  "<test>any</test>");

    JsonObject json = new JsonObject();
    json.addProperty("test", "value");
    Assert.assertEquals(Var.valueOf(json).getObjectAsString(),  "{\"test\":\"value\"}");

    JsonElement jsonElement = new JsonPrimitive("test");
    Assert.assertEquals(Var.valueOf(jsonElement).getObjectAsString(),  "test");

    Var image = new Var("MinhaImagem".getBytes());
    Assert.assertEquals(image.getObjectAsString(), "TWluaGFJbWFnZW0=");

    AnyObject obj = new AnyObject();
    obj.setTest("value");

    Assert.assertEquals(Var.valueOf(obj).getObjectAsString(),  "{\"test\":\"value\"}");
  }
}
