package cronapi.object;

import cronapi.CronapiMetaData;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;
import cronapi.ParamMetaData;
import cronapi.Var;

/**
 * Classe que representa ...
 * 
 * @author Usuário de Teste
 * @version 1.0
 * @since 2017-07-06
 *
 */
@CronapiMetaData(category = CategoryType.OBJECT, categoryTags = { "Object", "Objeto" })
public class Operations {
  
  	@CronapiMetaData(type = "function", name = "{{getObjectFieldName}}", nameTags = {
			"getObjectFieldName" }, description = "{{getObjectFieldDescription}}", returnType = ObjectType.OBJECT)
	public static final Var getObjectField(
			@ParamMetaData(type = ObjectType.OBJECT, description = "{{getObjectFieldParam0}}") Var objVar,
			@ParamMetaData(type = ObjectType.STRING, description = "{{getObjectFieldParam1}}") Var keyVar)
			throws Exception {
		return cronapi.json.Operations.getJsonOrMapField(objVar, keyVar);
	}

	@CronapiMetaData(type = "function", name = "{{setObjectFieldName}}", nameTags = {
			"seObjectField" }, description = "{{setObjectFieldDescription}}", returnType = ObjectType.VOID)
	public static final void setObjectField(
			@ParamMetaData(type = ObjectType.OBJECT, description = "{{setObjectFieldParam0}}") Var objVar,
			@ParamMetaData(type = ObjectType.STRING, description = "{{setObjectFieldParam1}}") Var keyVar,
			@ParamMetaData(type = ObjectType.OBJECT, description = "{{setObjectFieldParam2}}") Var value)
			throws Exception {
		cronapi.json.Operations.setJsonOrMapField(objVar, keyVar, value);
	}

}
