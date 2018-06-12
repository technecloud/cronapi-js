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
    Assert.assertFalse(Operations.isNull(Var.VAR_EMPTY).getObjectAsBoolean());
    Assert.assertTrue(Operations.isNull(null).getObjectAsBoolean());
  }

  @Test
  public void checkMethodIsNullOrEmpty() {
    List<Object> list = new ArrayList<>();
    list.add(new Object());
    Var listVar = Var.valueOf(list);
    Assert.assertFalse(Operations.isNullOrEmpty(listVar).getObjectAsBoolean());
    Assert.assertTrue(Operations.isNullOrEmpty(Var.VAR_EMPTY).getObjectAsBoolean());
    Assert.assertTrue(Operations.isNullOrEmpty(null).getObjectAsBoolean());
    Assert.assertFalse(Operations.isNullOrEmpty(Var.VAR_ZERO).getObjectAsBoolean());
  }

  @Test
  public void checkMethodIsEmpty() {
    List<Object> list = new ArrayList<>();
    list.add(new Object());
    Var listVar = Var.valueOf(list);
    Assert.assertFalse(Operations.isEmpty(listVar).getObjectAsBoolean());
    Assert.assertTrue(Operations.isEmpty(Var.VAR_EMPTY).getObjectAsBoolean());
    Assert.assertFalse(Operations.isEmpty(null).getObjectAsBoolean());
    Assert.assertFalse(Operations.isEmpty(Var.VAR_ZERO).getObjectAsBoolean());
  }

}
