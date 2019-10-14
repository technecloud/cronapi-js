package br.com.cronapi.text;

import cronapi.Var;
import cronapi.text.Operations;
import org.junit.Assert;
import org.junit.Test;

public class OperationsTest {

    @Test
    public void replaceNull() {
        Assert.assertNull(Operations.replace(Var.VAR_NULL, Var.VAR_NULL, Var.VAR_NULL).getObject());
        Assert.assertNull(Operations.replace(Var.valueOf("teste x testes"), Var.VAR_NULL, Var.VAR_NULL).getObject());
        Assert.assertNull(Operations.replace(Var.valueOf("teste x testes"), Var.valueOf("x"), Var.VAR_NULL).getObject());
        Assert.assertNull(Operations.replace(Var.VAR_NULL, Var.valueOf("x"), Var.valueOf("teste x testes")).getObject());
        Assert.assertNull(Operations.replace(Var.valueOf("teste x testes"), Var.VAR_NULL, Var.valueOf("x")).getObject());
    }

    @Test
    public void replaceTextEmpty() {
        Assert.assertNull(Operations.replace(Var.valueOf(" "), Var.valueOf(" "), Var.valueOf(" ")).getObject());
        Assert.assertNull(Operations.replace(Var.valueOf("teste"), Var.valueOf(" "), Var.valueOf(" ")).getObject());
    }

    @Test
    public void replaceText() {
        Assert.assertEquals(Operations.replace(Var.valueOf("teste x"), Var.valueOf("x"), Var.valueOf("teste")).getObjectAsString(), "teste teste");
    }

    @Test
    public void replaceTextAll() {
        Assert.assertEquals(Operations.replaceAll(Var.valueOf("/users/{id}/books/{id_book}"), Var.valueOf("\\{\\w*\\}"), Var.valueOf("\\\\w*")).getObjectAsString(), "/users/\\w*/books/\\w*");
    }

    @Test
    public void replaceTextFirst() {
        Assert.assertEquals(Operations.replaceFirst(Var.valueOf("cronapp.net"), Var.valueOf("net"), Var.valueOf("io")).getObjectAsString(), "cronapp.io");
        Assert.assertEquals(Operations.replaceFirst(Var.valueOf("acb.net"), Var.valueOf("ac(.*)"), Var.valueOf("cronapp")).getObjectAsString(), "cronapp");
    }
}