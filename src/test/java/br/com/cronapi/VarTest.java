package br.com.cronapi;

import cronapi.Var;
import org.testng.Assert;
import org.testng.annotations.Test;

public class VarTest {
  
    @Test
    public void nullVarShouldReturnEmptyString()
    {
        Assert.assertEquals(Var.VAR_NULL.getObjectAsString(), "");
        Assert.assertEquals(Var.valueOf(null).getObjectAsString(), "");
    }
}
