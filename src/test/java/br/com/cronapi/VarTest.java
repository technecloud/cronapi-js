package br.com.cronapi;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import cronapi.Utils;
import cronapi.Var;
import cronapi.i18n.Messages;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.*;

public class VarTest {

  private static TimeZone last;

  @BeforeClass
  public static void before() {
    last = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Messages.set(Locale.ENGLISH);
  }

  @AfterClass
  public static void after() {
    TimeZone.setDefault(last);
  }

  @Test
  public void nullVarShouldReturnEmptyString() {
    Assert.assertEquals(Var.VAR_NULL.getObjectAsString(), "");
    Assert.assertEquals(Var.valueOf(null).getObjectAsString(), "");
  }

  @Test
  public void testObject() {
    Map<String, String> map = new LinkedHashMap<>();
    map.put("key", "value");
    Var var = Var.valueOf(map);
    Assert.assertEquals(var.get("key"), "value");
    var.set("key2", "value2");
    Assert.assertEquals(var.get("key2"), "value2");
    Assert.assertEquals(Var.valueOf("primitive").get("anyKey"), "primitive");
    Assert.assertEquals(Var.valueOf(1L).get("anyKey"), 1L);
    Assert.assertEquals(Var.valueOf(true).get("anyKey"), true);
    Assert.assertEquals(getTestDate().get("anyKey"), getTestDate().getObject());

    AnyObject any = new AnyObject();
    any.setTest("value");

    Assert.assertEquals(Var.valueOf(any).get("test"), "value");
    any.setTest("value");

    Var anyVar = Var.valueOf(any);
    anyVar.set("test", "other");
    Assert.assertEquals(Var.valueOf(any).get("test"), "other");
  }

  @Test
  public void listConversion() {
    List list = Var.valueOf("1,2").getObjectAsList();

    Assert.assertEquals(list.get(0), "1");
    Assert.assertEquals(list.get(1), "2");

    Assert.assertEquals(list.get(0).getClass(), String.class);
    Assert.assertEquals(list.get(1).getClass(), String.class);

    list = Var.valueOf(1).getObjectAsList();
    Assert.assertEquals(list.get(0), 1L);
    Assert.assertEquals(list.get(0).getClass(), Long.class);
  }

  @Test
  public void generalConversion() {
    Assert.assertEquals((int) Var.valueOf("1").getObjectAsInt(), 1);
    Assert.assertEquals((int) Var.valueOf(1.0).getObjectAsInt(), 1);
    Assert.assertEquals((int) Var.valueOf(1.1).getObjectAsInt(), 1);
    Assert.assertEquals((double) Var.valueOf("1.0").getObjectAsDouble(), 1.0, 0.001);
    Assert.assertEquals((double) Var.valueOf(1).getObjectAsDouble(), 1.0, 0.001);

    Calendar calendar = Calendar.getInstance();
    calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
    calendar.set(Calendar.MILLISECOND, 0);

    Assert.assertEquals(Var.valueOf(Var.valueOf(calendar.getTime()).toString()).getObjectAsDateTime().getTimeInMillis(), calendar.getTimeInMillis());

    ISO8601DateFormat format = new ISO8601DateFormat();
    String iso = format.format(calendar.getTime());

    Assert.assertEquals(Var.valueOf(iso).getObjectAsDateTime().getTimeInMillis(), calendar.getTimeInMillis());

  }

  @Test
  public void dateConversion() {
    Assert.assertTrue(Var.deserialize("2019-04-10T00:00:00.000Z") instanceof Date);
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
    Assert.assertEquals(Var.valueOf("12").getObjectAsString(), "12");
    Assert.assertEquals(Var.valueOf("value").getObjectAsString(), "value");
    Assert.assertEquals(Var.valueOf(12).getObjectAsString(), "12");
    Assert.assertEquals(Var.valueOf(12.14556896).getObjectAsString(), "12.14556896");
    Assert.assertEquals(Var.valueOf(12.1).getObjectAsString(), "12.1");
    Assert.assertEquals(Var.valueOf(0.1).getObjectAsString(), "0.1");
    Assert.assertEquals(Var.valueOf(true).getObjectAsString(), "true");
    Assert.assertEquals(Var.valueOf(false).getObjectAsString(), "false");

    Date current = new Date();

    Assert.assertEquals(Var.valueOf(current).getObjectAsString(), Utils.getISODateFormat().format(current));
    Assert.assertEquals(Var.valueOf(getTestDate()).getObjectAsString(), "2012-05-12T04:05:24Z");
    Assert.assertEquals(Var.valueOf(new File("/tmp/test.txt")).getObjectAsString(), "/tmp/test.txt");

    ByteArrayInputStream stream = new ByteArrayInputStream("test".getBytes());
    Assert.assertEquals(Var.valueOf(stream).getObjectAsString(), "test");

    Document doc = new Document();
    Element newElement = new Element("test");
    newElement.setText("any");
    doc.addContent(newElement);

    Assert.assertEquals(Var.valueOf(doc).getObjectAsString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<test>any</test>\r\n");

    Assert.assertEquals(Var.valueOf(newElement).getObjectAsString(), "<test>any</test>");

    JsonObject json = new JsonObject();
    json.addProperty("test", "value");
    Assert.assertEquals(Var.valueOf(json).getObjectAsString(), "{\"test\":\"value\"}");

    JsonElement jsonElement = new JsonPrimitive("test");
    Assert.assertEquals(Var.valueOf(jsonElement).getObjectAsString(), "test");

    Var image = new Var("MinhaImagem".getBytes());
    Assert.assertEquals(image.getObjectAsString(), "TWluaGFJbWFnZW0=");

    AnyObject obj = new AnyObject();
    obj.setTest("value");

    Assert.assertEquals(Var.valueOf(obj).getObjectAsString(), "{\"test\":\"value\"}");
  }
}
