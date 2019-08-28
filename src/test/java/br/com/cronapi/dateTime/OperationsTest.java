package br.com.cronapi.dateTime;

import cronapi.Var;
import cronapi.dateTime.Operations;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.TimeZone;

public class OperationsTest {

  private TimeZone last;

  @BeforeClass
  private void before() {
    last = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @AfterClass
  private void after() {
    TimeZone.setDefault(last);
  }

  private Var getTestDate() {
    return Var.valueOf("2012-05-12T04:05:24Z");
  }

  private Var getSecondTestDate() {
    return cronapi.dateTime.Operations.newDate(Var.valueOf(2012), Var.valueOf(5), Var.valueOf(12), Var.valueOf(4), Var.valueOf(6), Var.valueOf(24));
  }

  private Var getThirdTestDate() {
    return cronapi.dateTime.Operations.newDate(Var.valueOf(2012), Var.valueOf(5), Var.valueOf(13), Var.valueOf(4), Var.valueOf(5), Var.valueOf(24));
  }

  private Var getFourthTestDate() {
    return cronapi.dateTime.Operations.newDate(Var.valueOf(2012), Var.valueOf(6), Var.valueOf(12), Var.valueOf(4), Var.valueOf(5), Var.valueOf(24));
  }

  private Var getFifthTestDate() {
    return cronapi.dateTime.Operations.newDate(Var.valueOf(2014), Var.valueOf(5), Var.valueOf(12), Var.valueOf(4), Var.valueOf(5), Var.valueOf(24));
  }

  @Test
  public void testUpdateDate() {
    Var item = getTestDate();
    item = cronapi.dateTime.Operations.updateDate(item, Var.valueOf(2012), Var.valueOf(5), Var.valueOf(12), Var.valueOf(12), Var.valueOf(5), Var.valueOf(24), Var.valueOf(100));
    Assert.assertNotNull(item);
  }

  @Test
  public void testGetParts() {
    Assert.assertEquals((int) cronapi.dateTime.Operations.getDay(getTestDate()).getObjectAsInt(), 12);
    Assert.assertEquals((int) cronapi.dateTime.Operations.getMonth(getTestDate()).getObjectAsInt(), 5);
    Assert.assertEquals((int) cronapi.dateTime.Operations.getYear(getTestDate()).getObjectAsInt(), 2012);
    Assert.assertEquals((int) cronapi.dateTime.Operations.getHour(getTestDate()).getObjectAsInt(), 4);
    Assert.assertEquals((int) cronapi.dateTime.Operations.getMinute(getTestDate()).getObjectAsInt(), 5);
    Assert.assertEquals((int) cronapi.dateTime.Operations.getSecond(getTestDate()).getObjectAsInt(), 24);
    Assert.assertEquals((int) cronapi.dateTime.Operations.getDayOfWeek(getTestDate()).getObjectAsInt(), 7);
  }

  @Test
  public void testDiffDates() {
    Assert.assertEquals((int) cronapi.dateTime.Operations.getSecondsBetweenDates(getSecondTestDate(), getTestDate()).getObjectAsInt(), 60);
    Assert.assertEquals((int) cronapi.dateTime.Operations.getMinutesBetweenDates(getSecondTestDate(), getTestDate()).getObjectAsInt(), 1);
    Assert.assertEquals((int) cronapi.dateTime.Operations.getHoursBetweenDates(getSecondTestDate(), getTestDate()).getObjectAsInt(), 0);
    Assert.assertEquals((int) cronapi.dateTime.Operations.getDaysBetweenDates(getThirdTestDate(), getTestDate()).getObjectAsInt(), 1);
    Assert.assertEquals((int) cronapi.dateTime.Operations.getMonthsBetweenDates(getFourthTestDate(), getTestDate()).getObjectAsInt(), 1);
    Assert.assertEquals((int) cronapi.dateTime.Operations.getYearsBetweenDates(getFifthTestDate(), getTestDate()).getObjectAsInt(), 2);
  }

  @Test
  public void testInc() {
    Var date = cronapi.dateTime.Operations.incSeconds(getTestDate(), Var.valueOf(1));
    Assert.assertEquals((int) Operations.getSecond(date).getObjectAsInt(), 25);
    date = cronapi.dateTime.Operations.incMinute(getTestDate(), Var.valueOf(1));
    Assert.assertEquals((int) Operations.getMinute(date).getObjectAsInt(), 6);
    date = cronapi.dateTime.Operations.incHour(getTestDate(), Var.valueOf(1));
    Assert.assertEquals((int) Operations.getHour(date).getObjectAsInt(), 5);
    date = cronapi.dateTime.Operations.incDay(getTestDate(), Var.valueOf(1));
    Assert.assertEquals((int) Operations.getDay(date).getObjectAsInt(), 13);
    date = cronapi.dateTime.Operations.incMonth(getTestDate(), Var.valueOf(1));
    Assert.assertEquals((int) Operations.getMonth(date).getObjectAsInt(), 6);
    date = cronapi.dateTime.Operations.incYear(getTestDate(), Var.valueOf(1));
    Assert.assertEquals((int) Operations.getYear(date).getObjectAsInt(), 2013);
  }

  @Test
  public void testForm() {
    Assert.assertEquals(Operations.formatDateTime(getTestDate(), Var.valueOf("dd/MM/yyyy HH:mm:ss")).getObjectAsString(), "12/05/2012 04:05:24");
  }
}
