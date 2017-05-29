package cronapi.json;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cronapi.CronapiMetaData;
import cronapi.ParamMetaData;
import cronapi.Utils;
import cronapi.Var;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;

@CronapiMetaData(category = CategoryType.JSON, categoryTags = { "Json" })
public class Operations {

	@CronapiMetaData(type = "function", name = "{{createObjectJson}}", nameTags = {
			"createObjectJson" }, description = "{{functionToCreateObjectJson}}", returnType = ObjectType.JSON)
	public static final Var createObjectJson() throws Exception {
		Var value = new Var(new JsonObject());
		return value;
	}

	@CronapiMetaData(type = "function", name = "{{getJsonOrMapField}}", nameTags = {
			"getJsonOrMapField" }, description = "{{functionToGetJsonOrMapField}}", returnType = ObjectType.OBJECT)
	public static final Var getJsonOrMapField(
			@ParamMetaData(type = ObjectType.OBJECT, description = "{{mapOrJsonVar}}") Var mapVar,
			@ParamMetaData(type = ObjectType.STRING, description = "{{pathKey}}") Var keyVar) throws Exception {
		Var value = Var.VAR_NULL;
		Object obj = mapVar.getObject();
		Object key = keyVar.getObject();

		String[] path = key.toString().split("\\.");
		for (int i = 0; i < path.length; i++) {
			String k = path[i];
			if (obj != null) {
				if (i == path.length - 1) {
					value = Var.valueOf(Utils.mapGetObjectPathExtractElement(obj, k, false));
				} else {
					obj = Utils.mapGetObjectPathExtractElement(obj, k, false);
				}
			}
		}
		return value;
	}

	@CronapiMetaData(type = "function", name = "{{setJsonOrMapField}}", nameTags = {
			"setJsonOrMapField" }, description = "{{functionToSetJsonOrMapField}}", returnType = ObjectType.VOID)
	public static final void setJsonOrMapField(
			@ParamMetaData(type = ObjectType.OBJECT, description = "{{mapOrJsonVar}}") Var mapVar,
			@ParamMetaData(type = ObjectType.STRING, description = "{{pathKey}}") Var keyVar,
			@ParamMetaData(type = ObjectType.OBJECT, description = "{{valueToBetSet}}") Var value) throws Exception {
		Object obj = mapVar.getObject();
		Object key = keyVar.getObject();

		String[] path = key.toString().split("\\.");
		for (int i = 0; i < path.length; i++) {
			String k = path[i];
			if (obj != null) {
				if (i == path.length - 1) {
					Utils.mapSetObject(obj, k, value);
				} else {
					obj = Utils.mapGetObjectPathExtractElement(obj, k, true);
				}
			}
		}
	}

	@CronapiMetaData(type = "function", name = "{{toJson}}", nameTags = {
			"toJson" }, description = "{{functionToJson}}", returnType = ObjectType.JSON)
	public static final Var toJson(
			@ParamMetaData(type = ObjectType.OBJECT, description = "{{valueToBeRead}}") Var valueToBeRead)
			throws Exception {
		Object obj = valueToBeRead.getObject();
		Gson c = new Gson();
		JsonElement json = null;
		if (obj instanceof String)
			json = c.fromJson(valueToBeRead.getObjectAsString(), JsonElement.class);
		else if (obj instanceof FileInputStream)
			json = c.fromJson(cronapi.io.Operations.fileReadAll(valueToBeRead).getObjectAsString(), JsonElement.class);
		return Var.valueOf(json);
	}

	@CronapiMetaData(type = "function", name = "{{toList}}", nameTags = { "toList",
			"Para Lista" }, description = "{{functionToList}}", returnType = ObjectType.LIST)
	public static final Var toList(
			@ParamMetaData(type = ObjectType.OBJECT, description = "{{valueToBeRead}}") Var valueToBeRead)
			throws Exception {
		return toMap(valueToBeRead);
	}

	@CronapiMetaData(type = "function", name = "{{toMap}}", nameTags = { "toMap",
			"Para Mapa" }, description = "{{functionToMap}}", returnType = ObjectType.MAP)
	public static final Var toMap(
			@ParamMetaData(type = ObjectType.OBJECT, description = "{{valueToBeRead}}") Var valueToBeRead)
			throws Exception {
		Object obj = null;
		String content = "";
		Gson c = new Gson();
		if (valueToBeRead.getObject() instanceof String)
			content = valueToBeRead.getObjectAsString();
		else if (valueToBeRead.getObject() instanceof FileInputStream)
			content = cronapi.io.Operations.fileReadAll(valueToBeRead).getObjectAsString();

		if (content.startsWith("["))
			obj = c.fromJson(content, List.class);
		else
			obj = c.fromJson(content, Map.class);

		return Var.valueOf(obj);
	}

}
