package br.com.cronapi.logic;

import cronapi.Var;
import cronapi.logic.Operations;
import java.util.ArrayList;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OperationsTest {

  @Test
  public void checkMethodIsNull() {
    Assert.assertEquals(Operations.isNull(Var.VAR_EMPTY), false);
    Assert.assertEquals(Operations.isNull(null), true);
  }

  @Test
  public void checkMethodIsNullOrEmpty() {
    List<Object> list = new ArrayList<>();
    list.add(new Object());
    Var listVar = Var.valueOf(list);
    Assert.assertEquals(Operations.isNullOrEmpty(listVar), false);
    Assert.assertEquals(Operations.isNullOrEmpty(Var.VAR_EMPTY), true);
    Assert.assertEquals(Operations.isNullOrEmpty(null), true);
    Assert.assertEquals(Operations.isNullOrEmpty(Var.VAR_ZERO), false);
  }

  @Test
  public void checkMethodIsEmpty() {
    List<Object> list = new ArrayList<>();
    list.add(new Object());
    Var listVar = Var.valueOf(list);
    Assert.assertEquals(Operations.isEmpty(listVar), false);
    Assert.assertEquals(Operations.isEmpty(Var.VAR_EMPTY), true);
    Assert.assertEquals(Operations.isEmpty(null), false);
    Assert.assertEquals(Operations.isEmpty(Var.VAR_ZERO), false);
  }

}
