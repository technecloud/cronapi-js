package br.com.cronapi.regex;

import cronapi.Var;
import cronapi.i18n.Messages;
import cronapi.regex.Operations;
import cronapi.regex.PatternFlags;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RegexTest {


    private String texto1;
    private String texto2;
    private String texto3;

    @Before
    public void setUp() throws Exception {
        texto1 = "estude$junit é muito$bom e bonito$demais é excelente";
        texto2 = "junit é muito importante e demais";
        texto3 = "the First line\nthe SecondLine";
    }



    @Test
    public void extractNullTextShouldBeReturnEmptyList() throws Exception{
        Var result = Operations.extractTextWithRegex(Var.VAR_NULL, Var.VAR_NULL, Var.VAR_NULL);
        Assert.assertTrue(result.getObjectAsList().isEmpty());

    }

    @Test
    public void extractEmptyTextShouldBeReturnEmptyList() throws Exception{
        Var result = Operations.extractTextWithRegex(Var.VAR_EMPTY, Var.VAR_NULL, Var.VAR_NULL);
        Assert.assertTrue(result.getObjectAsList().isEmpty());
    }


    @Test
    public void extractTextShouldBeReturnListOfList() throws Exception{
        Var result  = Operations.extractTextWithRegex(new Var(texto1), new Var("(\\w*)\\$(\\w*)"), Var.VAR_NULL);
        Assert.assertTrue(((result.getObjectAsList().get(0)) instanceof  List));

        Var result2 = Operations.extractTextWithRegex(new Var(texto2), new Var("(\\w{3,})"), Var.VAR_NULL);
        Assert.assertTrue(((result2.getObjectAsList().get(0)) instanceof  List));
    }

    @Test
    public void verifyListSize() throws Exception{
        Var result  = Operations.extractTextWithRegex(new Var(texto1), new Var("(\\w*)\\$(\\w*)"), Var.VAR_NULL);
        Assert.assertEquals(result.getObjectAsList().size(), 3);

        Var result2 = Operations.extractTextWithRegex(new Var(texto2), new Var("(\\w{3,})"), Var.VAR_NULL);
        Assert.assertEquals(result2.getObjectAsList().size(), 4);
    }

    @Test
    public void extractTextWithMultipleFlags() throws Exception{

        Var result  = Operations.extractTextWithRegex(new Var(texto3),
                new Var("^T.*e$"), new Var(Arrays.asList("CASE_INSENSITIVE","MULTILINE")));
        Assert.assertEquals(result.getObjectAsList().size(), 2);
    }


    @Test
    public void compareExtractText() throws Exception{
        Var result  = Operations.extractTextWithRegex(new Var(texto1), new Var("(\\w*)\\$(\\w*)"), Var.VAR_NULL);
        Assert.assertEquals(((List) ((result.getObjectAsList().get(0)))).get(0), "estude");
        Assert.assertEquals(((List) ((result.getObjectAsList().get(2)))).get(1), "demais");

        Var result2 = Operations.extractTextWithRegex(new Var(texto2), new Var("(\\w{3,})"), Var.VAR_NULL);
        Assert.assertEquals(((List) ((result2.getObjectAsList().get(0)))).get(0), "junit");
        Assert.assertEquals(((List) ((result2.getObjectAsList().get(3)))).get(0), "demais");
    }

    @Test
    public void validateTextNullShouldBeReturnFalse()  throws Exception{
            Assert.assertFalse(Operations.validateTextWithRegex(Var.VAR_NULL, Var.VAR_NULL, Var.VAR_NULL).getObjectAsBoolean());
    }

    @Test
    public void validateTextEmptyShouldBeReturnFalse()  throws Exception {
            Assert.assertFalse(Operations.validateTextWithRegex(Var.VAR_EMPTY, Var.VAR_NULL, Var.VAR_NULL).getObjectAsBoolean());
    }


    @Test
    public void validateTextWithoutFlagShouldBeReturnTrue()  throws Exception{
            Assert.assertTrue(Operations.validateTextWithRegex(new Var("123"), new Var("^\\d\\d\\d$"), Var.VAR_NULL).getObjectAsBoolean());
    }

    @Test
    public void validateTextCaseInsensitiveShouldBeReturnTrue() throws Exception{

            Assert.assertTrue(Operations.validateTextWithRegex(new Var("ABC"), new Var("^abc$"), new Var("CASE_INSENSITIVE")).getObjectAsBoolean());
            Assert.assertTrue(Operations.validateTextWithRegex(new Var("AbC"), new Var("^abc$"), new Var("CASE_INSENSITIVE")).getObjectAsBoolean());
            Assert.assertTrue(Operations.validateTextWithRegex(new Var("abc"), new Var("^abc$"), new Var("CASE_INSENSITIVE")).getObjectAsBoolean());
            Assert.assertTrue(Operations.validateTextWithRegex(new Var("aBc"), new Var("^abc$"), new Var("CASE_INSENSITIVE")).getObjectAsBoolean());
            Assert.assertTrue(Operations.validateTextWithRegex(new Var("abC"), new Var("^abc$"), new Var("CASE_INSENSITIVE")).getObjectAsBoolean());

    }


    @Test
    public void validateFlagShouldBeReturnException(){
        try {
            Assert.assertFalse(Operations.validateTextWithRegex(new Var("ABC"), new Var("^abc$"), new Var("ABC")).getObjectAsBoolean());
        } catch (Exception e) {
            Assert.assertEquals(e.getMessage(), Messages.getString("flagRegexError"));
        }
    }


    @Test
    public void validateTextWithoutCaseInsensitiveShouldBeReturnFalse() throws Exception {
            Assert.assertFalse(Operations.validateTextWithRegex(new Var("ABC"), new Var("^abc$"), Var.VAR_NULL).getObjectAsBoolean());
            Assert.assertFalse(Operations.validateTextWithRegex(new Var("AbC"), new Var("^abc$"), Var.VAR_NULL).getObjectAsBoolean());
            Assert.assertFalse(Operations.validateTextWithRegex(new Var("aBc"), new Var("^abc$"), Var.VAR_NULL).getObjectAsBoolean());
            Assert.assertFalse(Operations.validateTextWithRegex(new Var("abC"), new Var("^abc$"), Var.VAR_NULL).getObjectAsBoolean());

    }



}
