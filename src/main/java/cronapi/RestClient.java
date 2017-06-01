package cronapi;

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

  private RestBody body;
  
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
  
  public HttpServletRequest getRequest() {
    return request;
  }
  
  public HttpServletResponse getResponse() {
    return response;
  }
}
