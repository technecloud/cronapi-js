package cronapi;

import org.apache.http.client.methods.HttpRequestBase;

public class HttpGetWithBody extends HttpWithBody {

  public final static String METHOD_NAME = "GET";

  public HttpGetWithBody(final HttpRequestBase http) {
    super();
    setURI(http.getURI());
    this.setHeaders(http.getAllHeaders());
  }

  @Override
  public String getMethod() {
    return METHOD_NAME;
  }
}