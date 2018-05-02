package cronapi.logic;

import cronapi.CronapiMetaData;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;
import cronapi.ParamMetaData;
import cronapi.Var;

@CronapiMetaData(category = CategoryType.LOGIC, categoryTags = { "Lógica", "Logic" })
public class Operations {

	@CronapiMetaData(type = "function", name = "{{isNullName}}", nameTags = {
			"isNullFunction" }, description = "{{isNullDescription}}", displayInline = true, returnType = ObjectType.BOOLEAN)
	public static final Var isNull(@ParamMetaData(type = ObjectType.OBJECT, description = "{{parameter}}") Var var)
			throws Exception {
		return (var.equals(Var.VAR_NULL)) ? Var.VAR_TRUE : Var.VAR_FALSE;
	}

	@CronapiMetaData(type = "function", name = "{{isNullOrEmptyName}}", nameTags = {
			"isNullOrEmptyFunction" }, description = "{{isNullOrEmptyDescription}}", displayInline = true, returnType = ObjectType.BOOLEAN)
	public static final Var isNullOrEmpty(@ParamMetaData(type = ObjectType.OBJECT, description = "{{parameter}}") Var var)
			throws Exception {
		return (var.equals(Var.VAR_NULL) || var.getObjectAsString() == ""
				|| var.getObjectAsList().isEmpty()) ? Var.VAR_TRUE
						: Var.VAR_FALSE;
	}

	@CronapiMetaData(type = "function", name = "{{isEmptyName}}", nameTags = {
			"isEmptyFunction" }, description = "{{isEmptyDescription}}", displayInline = true, returnType = ObjectType.BOOLEAN)
	public static final Var isEmpty(@ParamMetaData(type = ObjectType.OBJECT, description = "{{parameter}}") Var var)
			throws Exception {
		return (var.getObjectAsString().isEmpty()
				|| (var.getType().equals(Var.Type.LIST) && var.getObjectAsList().isEmpty())) ? Var.VAR_TRUE
						: Var.VAR_FALSE;
	}

}