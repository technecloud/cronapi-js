package br.com.cronapi.math;

import cronapi.Var;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static cronapi.math.Operations.*;

public class OperationsTest {

    @BeforeMethod
    public void setUp() {
    }

    @AfterMethod
    public void tearDown() {
    }

    @Test
    public void testPow() throws Exception {
        // simple
        Assert.assertEquals(pow(Var.valueOf(7) ,Var.valueOf(2)).getObjectAsDouble(), Var.valueOf(49).getObjectAsDouble());
        Assert.assertEquals(pow(Var.valueOf(7) ,Var.valueOf(3)).getObjectAsDouble(), Var.valueOf(343).getObjectAsDouble());
        Assert.assertEquals(pow(Var.valueOf(2) ,Var.valueOf(10)).getObjectAsDouble(), Var.valueOf(1024).getObjectAsDouble());
        // fractional exponents
        Assert.assertEquals(pow(Var.valueOf(4) ,Var.valueOf(0.5)).getObjectAsDouble(), Var.valueOf(2).getObjectAsDouble());
        Assert.assertEquals(pow(Var.valueOf(8) ,Var.valueOf(0.333333333)).getObjectAsDouble(), Var.valueOf(1.9999999986137056).getObjectAsDouble());
        // signed exponents
        Assert.assertEquals(pow(Var.valueOf(7) ,Var.valueOf(-2)).getObjectAsDouble(), Var.valueOf(0.02040816326530612).getObjectAsDouble());
        // signed bases
        Assert.assertEquals(pow(Var.valueOf(-7) ,Var.valueOf(2)).getObjectAsDouble(), Var.valueOf(49).getObjectAsDouble());
        Assert.assertEquals(pow(Var.valueOf(-7) ,Var.valueOf(3)).getObjectAsDouble(), Var.valueOf(-343).getObjectAsDouble());
        Assert.assertEquals(pow(Var.valueOf(-7) ,Var.valueOf(0.5)).getObjectAsDouble(), Var.valueOf(Double.NaN).getObjectAsDouble());
    }
}