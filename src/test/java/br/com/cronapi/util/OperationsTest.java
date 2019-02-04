package br.com.cronapi.util;

import cronapi.Var;
import cronapi.util.Operations;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OperationsTest {

    @Test
    public void isUUID() {
        Var uuid = Operations.generateUUID();
        Assert.assertFalse(uuid.getObjectAsString().startsWith("\""));
    }
}
