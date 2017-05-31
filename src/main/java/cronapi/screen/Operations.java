package cronapi.screen;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;

import cronapi.*;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;
import cronapi.clazz.CronapiClassLoader;
import cronapi.i18n.Messages;

@CronapiMetaData(category = CategoryType.SCREEN, categoryTags = { "Tela", "Screen", "Frontend" })
public class Operations {

	@CronapiMetaData(type = "function", name = "{{getValueOfFieldName}}", nameTags = {
			"getValueOfField" }, description = "{{getValueOfFieldDescription}}", returnType = ObjectType.JSON)
	public static final Var getValueOfField(
			@ParamMetaData(blockType = "field_from_screen", type = ObjectType.OBJECT, description="{{getValueOfFieldParam0}}") Var field) throws Exception {
		return cronapi.map.Operations.getJsonOrMapField(Var.valueOf(RestClient.getRestClient().getBody().getFields()),
				field);
	}
}