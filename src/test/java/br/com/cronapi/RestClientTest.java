package br.com.cronapi;

import cronapi.RestClient;
import org.junit.Test;

public class RestClientTest {

    @Test
    public void checkGetRequest() {
        RestClient.getRestClient().getRequest();
    }

    @Test
    public void checkGetResponse() {
        RestClient.getRestClient().getResponse();
    }
}
