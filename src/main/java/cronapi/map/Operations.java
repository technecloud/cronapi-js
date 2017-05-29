package cronapi.map;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import cronapi.CronapiMetaData;
import cronapi.ParamMetaData;
import cronapi.Utils;
import cronapi.Var;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;

/**
 * Classe que representa ...
 * 
 * @author Usuário de Teste
 * @version 1.0
 * @since 2017-05-29
 *
 */
@CronapiMetaData(category = CategoryType.MAP, categoryTags = { "Map", "Mapa" })
public class Operations {

	@CronapiMetaData(type = "function", name = "{{createObjectWithMapName}}", nameTags = {
			"createObjectWithMap" }, description = "{{createObjectWithMapDescription}}", arbitraryParams = true, returnType = ObjectType.MAP)
	public static final Var createObjectMapWith(
			@ParamMetaData(type = ObjectType.OBJECT, description = "{{createObjectWithMapParam0}}") Var... map)
			throws Exception {
		LinkedHashMap<String, Object> mapObject = new LinkedHashMap<>();
		for (int i = 0; i < map.length; i++) {
			mapObject.put(map[i].getId(), map[i].getObject());
		}
		return new Var(mapObject);
	}

	@CronapiMetaData(type = "function", name = "{{createObjectMapName}}", nameTags = {
			"createObjectMap" }, description = "{{createObjectMapNameDescription}}", returnType = ObjectType.OBJECT)
	public static final Var createObjectMap() throws Exception {
		Var value = new Var(new HashMap<>());
		return value;
	}

	@CronapiMetaData(type = "function", name = "{{getMapFieldName}}", nameTags = {
			"getMapFieldName" }, description = "{{getMapFieldDescription}}", returnType = ObjectType.OBJECT)
	public static final Var getJsonOrMapField(
			@ParamMetaData(type = ObjectType.OBJECT, description = "{{getMapFieldParam0}}") Var mapVar,
			@ParamMetaData(type = ObjectType.STRING, description = "{{getMapFieldParam1}}") Var keyVar)
			throws Exception {
		return cronapi.json.Operations.getJsonOrMapField(mapVar, keyVar);
	}

	@CronapiMetaData(type = "function", name = "{{setMapFieldName}}", nameTags = {
			"setMapField" }, description = "{{setMapFieldDescription}}", returnType = ObjectType.VOID)
	public static final void setMapField(
			@ParamMetaData(type = ObjectType.OBJECT, description = "{{setMapFieldParam0}}") Var mapVar,
			@ParamMetaData(type = ObjectType.STRING, description = "{{setMapFieldParam1}}") Var keyVar,
			@ParamMetaData(type = ObjectType.OBJECT, description = "{{setMapFieldParam2}}") Var value)
			throws Exception {
		cronapi.json.Operations.setJsonOrMapField(mapVar, keyVar, value);
	}

	@CronapiMetaData(type = "function", name = "{{toJson}}", nameTags = {
			"toJson" }, description = "{{functionToJson}}", returnType = ObjectType.JSON)
	public static final Var toJson(
			@ParamMetaData(type = ObjectType.OBJECT, description = "{{valueToBeRead}}") Var valueToBeRead)
			throws Exception {
		return cronapi.json.Operations.toJson(valueToBeRead);
	}

	@CronapiMetaData(type = "function", name = "{{toList}}", nameTags = { "toList",
			"Para Lista" }, description = "{{functionToList}}", returnType = ObjectType.LIST)
	public static final Var toList(
			@ParamMetaData(type = ObjectType.OBJECT, description = "{{valueToBeRead}}") Var valueToBeRead)
			throws Exception {
		return cronapi.json.Operations.toList(valueToBeRead);
	}

	@CronapiMetaData(type = "function", name = "{{toMap}}", nameTags = { "toMap",
			"Para Mapa" }, description = "{{functionToMap}}", returnType = ObjectType.MAP)
	public static final Var toMap(
			@ParamMetaData(type = ObjectType.OBJECT, description = "{{valueToBeRead}}") Var valueToBeRead)
			throws Exception {
		return cronapi.json.Operations.toMap(valueToBeRead);
	}

}
