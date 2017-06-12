package cronapi.logic;

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
import cronapi.rest.CallBlocklyREST;

@CronapiMetaData(category = CategoryType.LOGIC, categoryTags = { "Lógica", "Logic" })
public class Operations {

	@CronapiMetaData(type = "function", name = "{{isNullName}}", nameTags = {
			"isNullFunction" }, description = "{{isNullDescription}}", returnType = ObjectType.BOOLEAN)
	public static final Var isNull(@ParamMetaData(type = ObjectType.STRING, description = "{{isNullParam0}}") Var var)
			throws Exception {
		return (var == Var.VAR_NULL) ? Var.VAR_TRUE : Var.VAR_FALSE;
	}

	@CronapiMetaData(type = "function", name = "{{isNullOrEmptyName}}", nameTags = {
			"isNullOrEmptyFunction" }, description = "{{isNullOrEmptyDescription}}", returnType = ObjectType.BOOLEAN)
	public static final Var isNullOrEmpty(
			@ParamMetaData(type = ObjectType.STRING, description = "{{isNullOrEmptyParam0}}") Var var)
			throws Exception {
		return (var == Var.VAR_NULL || var.getObjectAsString() == "") ? Var.VAR_TRUE : Var.VAR_FALSE;
	}

	@CronapiMetaData(type = "function", name = "{{isEmptyName}}", nameTags = {
			"isEmptyFunction" }, description = "{{isEmptyDescription}}", returnType = ObjectType.BOOLEAN)
	public static final Var isEmpty(@ParamMetaData(type = ObjectType.STRING, description = "{{isEmptyParam0}}") Var var)
			throws Exception {
		return (var != Var.VAR_NULL & var.getObjectAsString() == "") ? Var.VAR_TRUE : Var.VAR_FALSE;
	}
}