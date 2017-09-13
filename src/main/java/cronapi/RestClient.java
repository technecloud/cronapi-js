package cronapi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
	private JsonObject query = null;
	private boolean filteredEnabled = false;

	private static List<GrantedAuthority> DEFAULT_AUTHORITIES;

	static {
		DEFAULT_AUTHORITIES = new ArrayList<>();
		DEFAULT_AUTHORITIES.add(new SimpleGrantedAuthority("authenticated"));
	}

	private RestBody body;

	private Var rawBody;

	private TenantService tenantService;

	public static RestClient getRestClient() {
		RestClient restClient = REST_CLIENT.get();
		if (restClient == null) {
			restClient =  new RestClient();
			REST_CLIENT.set(restClient);
		}

		return restClient;
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

		Object user = null;

		if (SecurityContextHolder.getContext() != null
				&& SecurityContextHolder.getContext().getAuthentication() != null)
			user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		if (user instanceof User)
			return (User) user;
		else
			return null;
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
}
