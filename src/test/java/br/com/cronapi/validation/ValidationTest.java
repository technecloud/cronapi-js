package br.com.cronapi.validation;

import cronapi.Var;
import cronapi.i18n.Messages;
import cronapi.validation.Operations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class ValidationTest {

    @Before
    public void setUp() throws Exception {
    }


    @Test
    public void validateNullValue() throws Exception{
        Assert.assertFalse(Operations.validateCPF(Var.VAR_NULL).getObjectAsBoolean());
        Assert.assertFalse(Operations.validateCNPJ(Var.VAR_NULL).getObjectAsBoolean());
        Assert.assertFalse(Operations.validateEmail(Var.VAR_NULL).getObjectAsBoolean());
    }

    @Test
    public void validateEmptyValue() throws Exception{
        Assert.assertFalse(Operations.validateCPF(Var.VAR_EMPTY).getObjectAsBoolean());
        Assert.assertFalse(Operations.validateCNPJ(Var.VAR_EMPTY).getObjectAsBoolean());
        Assert.assertFalse(Operations.validateEmail(Var.VAR_EMPTY).getObjectAsBoolean());
    }

    @Test
    public void testInvalidCNPJAndCPFWithoutMask() throws Exception{
        Assert.assertFalse(Operations.validateCPF(new Var("12351122321")).getObjectAsBoolean());
        Assert.assertFalse(Operations.validateCPF(new Var("11111111111")).getObjectAsBoolean());
        Assert.assertFalse(Operations.validateCNPJ(new Var("123511223211236")).getObjectAsBoolean());
        Assert.assertFalse(Operations.validateCNPJ(new Var("188969966666666")).getObjectAsBoolean());
    }

    @Test
    public void testInvalidCNPJAndCPFWithMask() throws Exception{
        Assert.assertFalse(Operations.validateCPF(new Var("035.475.295-282")).getObjectAsBoolean());
        Assert.assertFalse(Operations.validateCPF(new Var("99.995.995-28")).getObjectAsBoolean());
        Assert.assertFalse(Operations.validateCNPJ(new Var("44.444.357/0991-38")).getObjectAsBoolean());
        Assert.assertFalse(Operations.validateCNPJ(new Var("41.268.357/0001-352")).getObjectAsBoolean());
    }

    @Test
    public void testValidCNPJAndCPFWithoutMask() throws Exception{
        Assert.assertTrue(Operations.validateCPF(new Var("55787664558")).getObjectAsBoolean());
        Assert.assertTrue(Operations.validateCPF(new Var("74661728149")).getObjectAsBoolean());
        Assert.assertTrue(Operations.validateCNPJ(new Var("86058271000113")).getObjectAsBoolean());
        Assert.assertTrue(Operations.validateCNPJ(new Var("79990214000102")).getObjectAsBoolean());
    }

    @Test
    public void testValidCNPJAndCPFWithMask() throws Exception{
        Assert.assertTrue(Operations.validateCPF(new Var("035.475.295-22")).getObjectAsBoolean());
        Assert.assertTrue(Operations.validateCPF(new Var("196.816.113-90")).getObjectAsBoolean());
        Assert.assertTrue(Operations.validateCNPJ(new Var("41.268.357/0001-32")).getObjectAsBoolean());
        Assert.assertTrue(Operations.validateCNPJ(new Var("70.106.682/0001-05")).getObjectAsBoolean());
    }

    @Test
    public void testValidEmail() throws Exception{
        Assert.assertTrue(Operations.validateEmail(new Var("cronapp@gmail.com")).getObjectAsBoolean());
        Assert.assertTrue(Operations.validateEmail(new Var("cronapp@cronapp.io")).getObjectAsBoolean());
        Assert.assertTrue(Operations.validateEmail(new Var("cronapp@outlook.com.br")).getObjectAsBoolean());
    }

    @Test
    public void testInvalidEmail() throws Exception{
        Assert.assertFalse(Operations.validateEmail(new Var("cro@napp@gmail.com.br")).getObjectAsBoolean());
        Assert.assertFalse(Operations.validateEmail(new Var("cronapp@")).getObjectAsBoolean());
        Assert.assertFalse(Operations.validateEmail(new Var("cronapp@br")).getObjectAsBoolean());
    }







}
