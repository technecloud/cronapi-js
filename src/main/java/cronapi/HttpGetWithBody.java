package cronapi;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;

public class HttpGetWithBody extends HttpEntityEnclosingRequestBase {

  public final static String METHOD_NAME = "GET";

  public HttpGetWithBody() { super(); }

  public HttpGetWithBody(final URI uri) {
    super();
    setURI(uri);
  }

  /**
   * @throws IllegalArgumentException if the uri is invalid.
   */
  public HttpGetWithBody(final String uri) {
    super();
    setURI(URI.create(uri));
  }

  @Override
  public String getMethod() {
    return METHOD_NAME;
  }
}