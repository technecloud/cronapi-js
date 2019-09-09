package br.com.cronapi;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import cronapi.Var;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.junit.Assert;
import org.junit.Test;

public class VarTest {

  @Test
  public void nullVarShouldReturnEmptyString() {
    Assert.assertEquals(Var.VAR_NULL.getObjectAsString(), "");
    Assert.assertEquals(Var.valueOf(null).getObjectAsString(), "");
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
  public void dateConversion(){
    Assert.assertTrue(Var.deserialize("2019-04-10T00:00:00.000Z") instanceof Date);
  }



}
