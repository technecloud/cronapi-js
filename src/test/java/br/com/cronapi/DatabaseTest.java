package br.com.cronapi;

import cronapi.Var;
import cronapi.database.Operations;
import org.junit.Assert;
import org.junit.Test;


public class DatabaseTest {
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
      Assert.assertFalse(e instanceof NullPointerException);
    }
  }
}
