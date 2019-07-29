package br.com.cronapi.conversion;

import cronapi.Var;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.GregorianCalendar;

public class Operations {


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
}
