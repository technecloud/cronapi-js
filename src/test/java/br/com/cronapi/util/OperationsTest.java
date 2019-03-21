package br.com.cronapi.util;

import cronapi.Var;
import cronapi.util.Operations;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class OperationsTest {

    @Test
    public void isUUID() {
        Var uuid = Operations.generateUUID();
        Assert.assertFalse(uuid.getObjectAsString().startsWith("\""));
    }

    @Test
    public void hasParams() {
      Var method = Var.valueOf("POST");
      //Var contentType = Var.valueOf("application/json");
      Var contentType = Var.valueOf("application/json");
      Var address = Var.valueOf("https://reqres.in/api/users/2");
      try {
      Var params = Var.valueOf(cronapi.map.Operations.createObjectMapWith(Var.valueOf("name", Var.valueOf("morpheus"))));
      //Var params = Var.VAR_NULL;
      Var cookieContainer = Var.valueOf(Var.VAR_NULL);
      Var postData = Var.valueOf(cronapi.map.Operations.createObjectMapWith(Var.valueOf("job", Var.valueOf("zion resident"))));
      //Var postData = Var.VAR_NULL;
      //Var postData = Var.valueOf("{\"LOGUSR\":\"ADMIN\",\"PSSUSER\":\"ADMIN\",\"CODUSR\":\"000197\",\"USRTKN\":\"\",\"CLIENT\":\"\",\"CAMPHA\":\"\",\"DTRANG\":\"\",\"STATUS\":\"\"}");
        Var data = Operations.getURLFromOthers(method, contentType, address, params, cookieContainer, postData);
        Assert.assertFalse(data.isEmptyOrNull());
      }catch (Exception e) {
      }
    }

    public static void main(){

    }
}
