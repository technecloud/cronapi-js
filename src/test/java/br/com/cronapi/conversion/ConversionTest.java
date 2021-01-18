package br.com.cronapi.conversion;

import cronapi.Var;
import cronapi.conversion.Operations;
import cronapi.i18n.Messages;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

public class ConversionTest {

  private static TimeZone last;

  @BeforeClass
  public static void before() {
    last = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Messages.set(new Locale("pt", "BR"));
  }

  @AfterClass
  public static void after() {
    TimeZone.setDefault(last);
  }

  private Calendar getTestDate() {
    return Var.valueOf("2012-05-12T04:05:24Z").getObjectAsDateTime();
  }

  @Test
  public void testConversion() {
    Assert.assertEquals(Operations.convert(Var.valueOf(1), Var.valueOf("STRING")).getObject(), "1");
    Assert.assertEquals(Operations.convert(Var.valueOf(true), Var.valueOf("STRING")).getObject(), "true");
    Assert.assertEquals(Operations.convert(Var.valueOf("true"), Var.valueOf("BOOLEAN")).getObject(), true);
    Assert.assertEquals(Operations.convert(Var.valueOf("false"), Var.valueOf("BOOLEAN")).getObject(), false);
    Assert.assertEquals(Operations.convert(Var.valueOf("2012-05-12T04:05:24Z"), Var.valueOf("DATETIME")).getObject(), getTestDate());

    // SHORT ASSERTION
   Assert.assertEquals(Operations.convert(Var.valueOf(1), Var.valueOf("SHORT")).getObject(), (short) 1);
   Assert.assertEquals(Operations.convert(Var.valueOf(1L), Var.valueOf("SHORT")).getObject(), (short) 1);
   Assert.assertEquals(Operations.convert(Var.valueOf("1"), Var.valueOf("SHORT")).getObject(), (short) 1);
   Assert.assertEquals(Operations.convert(Var.valueOf('1'), Var.valueOf("SHORT")).getObject(), (short) 1);
   Assert.assertEquals(Operations.convert(Var.valueOf(1.0), Var.valueOf("SHORT")).getObject(), (short) 1);
   Assert.assertEquals(Operations.convert(Var.valueOf("1.0"), Var.valueOf("SHORT")).getObject(), (short) 1);
   Assert.assertEquals(Operations.convert(Var.valueOf(true), Var.valueOf("SHORT")).getObject(), (short) 1);
   Assert.assertEquals(Operations.convert(Var.valueOf(false), Var.valueOf("SHORT")).getObject(), (short) 0);
   Assert.assertEquals(Operations.convert(Var.valueOf("true"), Var.valueOf("SHORT")).getObject(), (short) 1);
   Assert.assertEquals(Operations.convert(Var.valueOf(" true "), Var.valueOf("SHORT")).getObject(), (short) 1);
   Assert.assertEquals(Operations.convert(Var.valueOf("false"), Var.valueOf("SHORT")).getObject(), (short) 0);
   Assert.assertEquals(Operations.convert(Var.valueOf(null), Var.valueOf("SHORT")).getObject(), (short) 0);
   Assert.assertEquals(Operations.convert(Var.valueOf("null"), Var.valueOf("SHORT")).getObject(), (short) 0);

    Calendar cal = Calendar.getInstance();
    cal.setTime(getTestDate().getTime());
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);

    Assert.assertEquals(Operations.convert(Var.valueOf("2012-05-12T04:05:24Z"), Var.valueOf("DATE")).getObject(), cal);
    Assert.assertEquals(Operations.convert(Var.valueOf("2012-05-12T04:05:24Z"), Var.valueOf("TEXTTIME")).getObject(), "04:05:24");
    Assert.assertEquals(Operations.convert(Var.valueOf("2012-05-12T04:05:24Z"), Var.valueOf("TIME")).getObject(), Var.valueOf("1970-01-01T04:05:24Z").getObjectAsDateTime());
    Assert.assertEquals(Operations.convert(Var.valueOf("2012-05-12T04:05:24Z"), Var.valueOf("ISODATE")).getObject(), "2012-05-12T04:05:24Z");

    Assert.assertEquals(Operations.convert(Var.valueOf("1"), Var.valueOf("INTEGER")).getObject(), 1L);
    Assert.assertEquals(Operations.convert(Var.valueOf("1.1"), Var.valueOf("INTEGER")).getObject(), 1L);
    Assert.assertEquals(Operations.convert(Var.valueOf("1.1"), Var.valueOf("DOUBLE")).getObject(), 1.1D);
    Assert.assertEquals(Operations.convert(Var.valueOf("1"), Var.valueOf("DOUBLE")).getObject(), 1.0D);

    Map<String, String> map = new LinkedHashMap<>();
    map.put("Test", "Value");

    Assert.assertEquals(Operations.convert(Var.valueOf(map), Var.valueOf("MAP")).getObject(), map);

    List<String> list = new LinkedList<>();
    list.add("Test");

    Assert.assertEquals(((List) Operations.convert(Var.valueOf(list), Var.valueOf("LIST")).getObject()).get(0), list.get(0));

    Assert.assertEquals(((List) Operations.convert(Var.valueOf("[1,2]"), Var.valueOf("LIST")).getObject()).get(0), "1");
    Assert.assertEquals(((List) Operations.convert(Var.valueOf("1"), Var.valueOf("LIST")).getObject()).get(0), "1");

    byte[] test = "teste".getBytes();
    String b64 = new String(Base64.getEncoder().encode(test));

    Assert.assertEquals(Operations.convert(Var.valueOf(test), Var.valueOf("BYTEARRAY")).getObject(), test);
    Assert.assertEquals(new String((byte[]) Operations.convert(Var.valueOf(b64), Var.valueOf("BYTEARRAY")).getObject()), new String(test));
    Assert.assertEquals(new String((byte[]) Operations.convert(Var.valueOf("teste"), Var.valueOf("BYTEARRAY")).getObject()), new String(test));

    Assert.assertEquals(Var.valueOf("null").getObjectAsInt(), Integer.valueOf(0));
    Assert.assertEquals(Var.valueOf("null").getObjectAsLong(), Long.valueOf(0));
    Assert.assertEquals(Var.valueOf("null").getObjectAsDouble(), Double.valueOf(0));
    Assert.assertEquals(Var.valueOf("null").getObjectAsBoolean(), Boolean.FALSE);
    Assert.assertEquals(Var.valueOf("null").getObjectAsDateTime(), Var.VAR_DATE_ZERO.getObjectAsDateTime());
  }
}
