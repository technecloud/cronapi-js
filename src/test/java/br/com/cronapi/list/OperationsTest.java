package br.com.cronapi.list;


import static cronapi.json.Operations.GSON_CONFIGURATION;
import static cronapi.json.Operations.toJson;

import com.google.gson.JsonElement;
import com.jayway.jsonpath.JsonPath;
import cronapi.Var;
import cronapi.list.Operations;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OperationsTest {

  private Var BOOKS_JSON;

  @BeforeClass
  public void oneTimeSetUp() throws Exception {
    //
    try (InputStream booksInput = getClass().getResourceAsStream("/books.json")) {
      String json = JsonPath.using(GSON_CONFIGURATION)
          .parse(IOUtils.toString(booksInput))
          .read("$.store.book")
          .toString();
      BOOKS_JSON = toJson(Var.valueOf(json));
    }
  }

  @Test
  public void newListShouldReturnEmptyList() {
    Var newListVar = Operations.newList();
    Assert.assertTrue(newListVar.getObject() instanceof List);
    List newList = (List) newListVar.getObject();
    Assert.assertEquals(newList.size(), 0);
  }

  @Test
  void sizeFromListTest() {
    List<Object> list = new ArrayList<>();
    list.add(new Object());
    Var listVar = Var.valueOf(list);
    Assert.assertEquals(Operations.size(listVar).getObjectAsInt().intValue(), 1);
  }

  @Test
  void sizeFromJsonTest() {
    Var listVar = Var.valueOf(BOOKS_JSON);
    Assert.assertEquals(Operations.size(listVar).getObjectAsInt().intValue(), 4);
  }

}
