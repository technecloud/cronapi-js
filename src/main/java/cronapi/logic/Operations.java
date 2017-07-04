package cronapi.logic;

import cronapi.*;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;

@CronapiMetaData(category = CategoryType.LOGIC, categoryTags = { "Lógica", "Logic" })
public class Operations {

	@CronapiMetaData(type = "function", name = "{{isNullName}}", nameTags = {
			"isNullFunction" }, description = "{{isNullDescription}}", displayInline = true, returnType = ObjectType.BOOLEAN)
	public static final Var isNull(@ParamMetaData(type = ObjectType.OBJECT, description = "") Var var)
			throws Exception {
		return (var.equals(Var.VAR_NULL)) ? Var.VAR_TRUE : Var.VAR_FALSE;
	}

	@CronapiMetaData(type = "function", name = "{{isNullOrEmptyName}}", nameTags = {
			"isNullOrEmptyFunction" }, description = "{{isNullOrEmptyDescription}}", displayInline = true, returnType = ObjectType.BOOLEAN)
	public static final Var isNullOrEmpty(@ParamMetaData(type = ObjectType.OBJECT, description = "") Var var)
			throws Exception {
		return (var.equals(Var.VAR_NULL) || var.getObjectAsString() == ""
				|| (var.getType().equals(Var.Type.LIST) && var.getObjectAsList().isEmpty())) ? Var.VAR_TRUE
						: Var.VAR_FALSE;
	}

	@CronapiMetaData(type = "function", name = "{{isEmptyName}}", nameTags = {
			"isEmptyFunction" }, description = "{{isEmptyDescription}}", displayInline = true, returnType = ObjectType.BOOLEAN)
	public static final Var isEmpty(@ParamMetaData(type = ObjectType.OBJECT, description = "") Var var)
			throws Exception {
		return (var.getObjectAsString().isEmpty()
				|| (var.getType().equals(Var.Type.LIST) && var.getObjectAsList().isEmpty())) ? Var.VAR_TRUE
						: Var.VAR_FALSE;
	}

	public static void main(String... args) {

		try {
			System.out.println(Operations.isEmpty(Var.VAR_NULL));

			System.out.println(Operations.isEmpty(Var.valueOf("12")));

		} catch (Exception e) {
			System.out.println(e);
		}

	}

}