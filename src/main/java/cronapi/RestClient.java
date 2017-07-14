package cronapi;

import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedList;
import java.util.List;

public class RestClient {
  
  private static ThreadLocal<RestClient> REST_CLIENT = new ThreadLocal<RestClient>() {
    @Override
    protected RestClient initialValue() {
      return new RestClient();
    }
  };
  
  private LinkedList<ClientCommand> commands = new LinkedList<>();
  private HttpServletResponse response = CronapiFilter.RESPONSE.get();
  private HttpServletRequest request = CronapiFilter.REQUEST.get();
  private JsonObject query = null;

  private RestBody body;

  private Var rawBody;
  
  public static RestClient getRestClient() {
    return REST_CLIENT.get();
  }
  
  public static void removeClient() {
    REST_CLIENT.remove();
  }
  
  public ClientCommand addCommand(ClientCommand command) {
    commands.add(command);
    return command;
  }
  
  public ClientCommand addCommand(String name) {
    ClientCommand command = new ClientCommand(name);
    commands.add(command);
    return command;
  }
  
  public LinkedList<ClientCommand> getCommands() {
    return commands;
  }
  
  public RestBody getBody() {
    if(body == null)
      body = new RestBody();
    return body;
  }

  public void setBody(RestBody body) {
    this.body = body;
  }

  public Var getRawBody() {
    return rawBody;
  }

  public void setRawBody(Var rawBody) {
    this.rawBody = rawBody;
  }

  public HttpServletRequest getRequest() {
    return request;
  }
  
  public HttpServletResponse getResponse() {
    return response;
  }

  public String getParameter(String key) {
    return request.getParameter(key);
  }

  public String getMethod() {
    return request.getMethod();
  }

  public JsonObject getQuery() {
    return query;
  }

  public void setQuery(JsonObject query) {
    this.query = query;
  }
}
