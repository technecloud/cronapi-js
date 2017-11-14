package cronapi;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import cronapi.database.TransactionManager;
import cronapi.i18n.AppMessages;
import cronapi.i18n.Messages;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.google.gson.JsonObject;

import cronapi.database.TenantService;

public class RestClient {

	private static ThreadLocal<RestClient> REST_CLIENT = new ThreadLocal<RestClient>();

	private LinkedList<ClientCommand> commands = new LinkedList<>();
	private HttpServletResponse response = CronapiFilter.RESPONSE.get();
	private HttpServletRequest request = CronapiFilter.REQUEST.get();
	private HttpSession session;
	private User user;
	private JsonObject query = null;
	private boolean filteredEnabled = false;
	private Locale locale;

	private static List<GrantedAuthority> DEFAULT_AUTHORITIES;

	static {
		DEFAULT_AUTHORITIES = new ArrayList<>();
		DEFAULT_AUTHORITIES.add(new SimpleGrantedAuthority("authenticated"));
	}

	private RestBody body;

	private Var rawBody;

	private TenantService tenantService;

	public RestClient clone() {
    TenantService newTenant = new TenantService();

    if (tenantService != null) {
      newTenant.getContextIds().putAll(tenantService.getContextIds());
    }

    RestClient newClient = new RestClient();
    newClient.setUser(getUser());
    newClient.setTenantService(newTenant);
    newClient.setSession(getSession());
    newClient.setLocale(getLocale());
    newClient.setFilteredEnabled(filteredEnabled);

    return newClient;
  }

  public static Runnable getContextRunnable(final Runnable runnable, final boolean transactional) {
	  final RestClient client = getRestClient().clone();
	  return () -> {
	    RestClient.setRestClient(client);
	    try {
	      if (transactional)
	        contextExecute(runnable);
	      else
          runnable.run();
      } finally {
        RestClient.removeClient();
        Messages.remove();
        AppMessages.remove();
      }
    };
  }

  private static void contextExecute(Runnable runnable) {

    try {
      runnable.run();
      TransactionManager.commit();
    } catch (Exception e) {
      TransactionManager.rollback();
      throw new RuntimeException(e);
    } finally {
      TransactionManager.close();
      TransactionManager.clear();
    }
  }

	public static RestClient getRestClient() {
		RestClient restClient = REST_CLIENT.get();
		if (restClient == null) {
			restClient =  new RestClient();
			REST_CLIENT.set(restClient);
		}

		return restClient;
	}
	
	public static void setRestClient(RestClient client) {
    REST_CLIENT.set(client);

    if (client.getLocale() != null) {
      Messages.set(client.getLocale());
      AppMessages.set(client.getLocale());
    }
  }

	public static void removeClient() {
		REST_CLIENT.set(null);
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
		if (body == null)
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
	   if (request == null)
	      request = CronapiFilter.REQUEST.get();
		return request.getMethod();
	}

	public JsonObject getQuery() {
		return query;
	}

	public void setQuery(JsonObject query) {
		this.query = query;
	}

	public User getUser() {
	  if (user != null)
      return user;
	  else {
      Object localUser = null;

      if (SecurityContextHolder.getContext() != null
          && SecurityContextHolder.getContext().getAuthentication() != null)
        localUser = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

      if (localUser instanceof User)
        user = (User) localUser;

      return user;
    }
	}

	public void setUser(User user) {
	  this.user = user;
  }

	public Collection<GrantedAuthority> getAuthorities() {
		User user = getUser();
		if (user != null)
			return user.getAuthorities();

		return Collections.EMPTY_LIST;
	}

	public boolean isFilteredEnabled() {
		return filteredEnabled;
	}

	public void setFilteredEnabled(boolean filteredEnabled) {
		this.filteredEnabled = filteredEnabled;
	}

	public TenantService getTenantService() {
		return tenantService;
	}

	public void setTenantService(TenantService tenantService) {
		this.tenantService = tenantService;
	}

  public HttpSession getSession() {
	  if (session != null) {
      return session;
    } else {
	    if (request != null)
	      return request.getSession();
    }

    return null;
  }

  public void setSession(HttpSession session) {
	  this.session = session;
  }

  public void updateSessionValue(String name, Object value) {
    getSession().setAttribute(name, value);
	}

  public Locale getLocale() {
	  if (locale != null)
      return locale;
    else {
      if (request != null)
        return request.getLocale();
    }

    return Messages.DEFAUL_LOCALE;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  public Object getSessionValue(String name) {
	  return getSession().getAttribute(name);
	}
}
