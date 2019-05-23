package br.com.cronapi;

import cronapi.Var;
import cronapi.database.Operations;
import org.testng.Assert;
import org.testng.annotations.Test;


public class DatabaseTests {
  @Test
  public void QAIBT_1436() {
    try {
      Operations.executeQuery(
          Var.valueOf("app.entity.User"),
          Var.valueOf("select u from User u"),
          Var.VAR_NULL
      );
      Assert.fail();
    } catch (Exception e) {
      Assert.assertFalse(e instanceof NullPointerException, "Should not throw NullPointerException");
    }
  }
}
