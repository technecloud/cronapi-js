package cronapi.screen;

import cronapi.*;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CronapiMetaData(category = CategoryType.SCREEN, categoryTags = { "Formulário", "Form", "Frontend" })
public class Operations {

	@CronapiMetaData(type = "function", name = "{{getValueOfFieldName}}", nameTags = {
			"getValueOfField" }, description = "{{getValueOfFieldDescription}}", returnType = ObjectType.JSON)
  public static final Var getValueOfField(
      @ParamMetaData(blockType = "field_from_screen", type = ObjectType.STRING, description="{{getValueOfFieldParam0}}") Var field) throws Exception {
    Var result = cronapi.map.Operations.getJsonOrMapField(Var.valueOf(RestClient.getRestClient().getBody().getFields()),
        field);

    if (result.getObject() instanceof String) {
      result = Var.valueOf(Var.deserialize((String) result.getObject()));
    }

    return result;
  }

  @CronapiMetaData(type = "function", name = "{{getParamFromQueryString}}", nameTags = {
      "parameter", "Parametro", "request", "requisição"}, description = "{{getParamDescription}}", returnType = ObjectType.STRING)
  public static final Var getParam(
      @ParamMetaData( type = ObjectType.STRING, description="{{getValueOfFieldParam0}}") Var name) throws Exception {
    Var result =  Var.valueOf(RestClient.getRestClient().getParameter(name.getObjectAsString()));

    if (result.getObject() instanceof String) {
      result = Var.valueOf(Var.deserialize((String) result.getObject()));
    }

    return result;
  }

  @CronapiMetaData(type = "function", name = "{{getHeader}}", nameTags = {
      "header", "cabeçalho", "request", "requisição" }, description = "{{getHeaderDescription}}", returnType = ObjectType.STRING)
  public static final Var getHeader(
      @ParamMetaData( type = ObjectType.STRING, description="{{getValueOfFieldParam0}}") Var name) throws Exception {
    Var result =  Var.valueOf(RestClient.getRestClient().getHeader(name.getObjectAsString()));
    return result;
  }

  @CronapiMetaData(type = "function", name = "{{getToken}}", nameTags = {
      "token", "requisição", "request" }, description = "{{getTokenDescription}}", returnType = ObjectType.STRING)
  public static final Var getToken() throws Exception {
    Var result =  Var.valueOf(RestClient.getRestClient().getHeader(TokenUtils.AUTH_HEADER_NAME));
    return result;
  }

  @CronapiMetaData(type = "function", name = "{{getTokenClaim}}", nameTags = {
      "token", "claim" }, description = "{{getTokenClaimDescription}}", returnType = ObjectType.STRING)
  public static final Var getTokenClaim(
      @ParamMetaData( type = ObjectType.STRING, description="{{getTokenClaimKey}}") Var key) throws Exception {
	  Claims claims = TokenUtils.getClaimsFromToken(RestClient.getRestClient().getHeader(TokenUtils.AUTH_HEADER_NAME));

    return Var.valueOf(claims.get(key.getObjectAsString()));
  }

  @CronapiMetaData(type = "function", name = "{{getTokenClaims}}", nameTags = {
      "token", "claim" }, description = "{{getTokenClaimsDescription}}", returnType = ObjectType.STRING)
  public static final Var getTokenClaims() throws Exception {
    Claims claims = TokenUtils.getClaimsFromToken(RestClient.getRestClient().getHeader(TokenUtils.AUTH_HEADER_NAME));
    Var result = Var.valueOf(new HashMap<>());
    for (Map.Entry entry: claims.entrySet()) {
      result.put(entry.getKey(), Var.valueOf(entry.getValue()));
    }
    return result;
  }

  @CronapiMetaData(type = "function", name = "{{addTokenClaim}}", nameTags = {
      "token", "claim" }, description = "{{addTokenClaimDescription}}", returnType = ObjectType.STRING)
  public static final void addTokenClaim(
      @ParamMetaData( type = ObjectType.STRING, description="{{addTokenClaimKey}}") Var key,
      @ParamMetaData( type = ObjectType.STRING, description="{{addTokenClaimValue}}") Var value) throws Exception {
	  boolean fromAuth = false;
	  for (StackTraceElement element: Thread.currentThread().getStackTrace()) {
	    if (element.getClassName().equals("cronapp.framework.authentication.token.AuthenticationController")) {
        fromAuth = true;
        break;
      }
    }
	  if (fromAuth) {
      RestClient.getRestClient().getRequest().setAttribute("CronappToken:" + key.getObjectAsString(), value.getObjectAsString());
    } else {
      String token = RestClient.getRestClient().getRequest().getHeader(TokenUtils.AUTH_HEADER_NAME);
      if (StringUtils.isNotEmpty(token)) {
        String newToken = TokenUtils.addClaimToToken(token, key.getObjectAsString(), value.getObjectAsString());
        ClientCommand command = new ClientCommand("cronapi.util.setToken");
        command.addParam(newToken);

        RestClient.getRestClient().addCommand(command);
      } else {
        throw new RuntimeException("Token is not in the header");
      }
    }
  }

}
