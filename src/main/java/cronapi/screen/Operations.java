package cronapi.screen;

import cronapi.*;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;

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
}