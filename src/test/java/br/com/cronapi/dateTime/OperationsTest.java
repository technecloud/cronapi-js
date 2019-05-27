package br.com.cronapi.dateTime;

import cronapi.Var;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OperationsTest {

  private Var item = Var.VAR_NULL;

  @Test
  public void testUpdateDate() {
    try {
      item = cronapi.dateTime.Operations.newDate(Var.valueOf(2012), Var.valueOf(5), Var.valueOf(12), Var.valueOf(4), Var.valueOf(5), Var.valueOf(24));
      item = cronapi.dateTime.Operations.updateDate(item, Var.valueOf(2012), Var.valueOf(5), Var.valueOf(12), Var.valueOf(12), Var.valueOf(5), Var.valueOf(24), Var.valueOf(100));
      Assert.assertNotNull(item);
    }catch(Exception e){
      Assert.assertFalse(e instanceof NullPointerException, "Should not throw NullPointerException");
    }
  }
}
