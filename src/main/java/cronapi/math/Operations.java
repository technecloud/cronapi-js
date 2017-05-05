package cronapi.math;

import cronapi.CronapiMetaData;
import cronapi.Var;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;

/**
 * Classe que representa operações matemáticas
 * 
 * @author Rodrigo Santos Reis
 * @version 1.0
 * @since 2017-05-04
 *
 */
@CronapiMetaData(category = CategoryType.UTIL, categoryTags = { "Math", "Matemática" })
public class Operations {

	@CronapiMetaData(type = "function", name = "{{multiplyFunctionName}}", nameTags = {
			"multiplyFunction" }, description = "{{multiplyFunctionDescription}}", params = {
					"{{multiplyFunctionParam0}}" }, paramsType = {
							ObjectType.OBJECT }, returnType = ObjectType.OBJECT, arbitraryParams = true)
	public static final Var multiply(Var... values) throws Exception {
		Var result = new Var();

		switch (values[0].getType()) {
		case DOUBLE: {
			result = multiplyDouble(values);
			break;
		}
		case LONG: {
			result = multiplyLong(values);
			break;
		}
		default: {
			result = multiplyDouble(values);
		}

		}
		return result;
	}

	@CronapiMetaData(type = "function", name = "{{subtractFunctionName}}", nameTags = {
			"subtractFunction" }, description = "{{subtractFunctionDescription}}", params = {
					"{{subtractFunctionParam0}}" }, paramsType = {
							ObjectType.OBJECT }, returnType = ObjectType.OBJECT, arbitraryParams = true)
	public static final Var subtract(Var... values) throws Exception {
		Var result = new Var();

		switch (values[0].getType()) {
		case DOUBLE: {
			result = subtractDouble(values);
			break;
		}
		case LONG: {
			result = subtractLong(values);
			break;
		}
		default: {
			result = subtractDouble(values);
		}

		}
		return result;
	}

	@CronapiMetaData(type = "function", name = "{{addFunctionName}}", nameTags = {
			"addFunction" }, description = "{{addFunctionDescription}}", params = {
					"{{addFunctionParam0}}" }, paramsType = {
							ObjectType.OBJECT }, returnType = ObjectType.OBJECT, arbitraryParams = true)
	public static final Var add(Var... values) throws Exception {
		Var result = new Var();

		switch (values[0].getType()) {
		case DOUBLE: {
			result = addDouble(values);
			break;
		}
		case LONG: {
			result = addLong(values);
			break;
		}
		default: {
			result = addDouble(values);
		}

		}
		return result;
	}

	@CronapiMetaData(type = "function", name = "{{addLongName}}", nameTags = {
			"addLong" }, description = "{{addLongDescription}}", params = { "{{addLongParam0}}" }, paramsType = {
					ObjectType.OBJECT }, returnType = ObjectType.LONG, arbitraryParams = true)
	public static final Var addLong(Var... values) throws Exception {
		Long addedValue = 0L;
		for (Var value : values) {
			addedValue += value.getObjectAsLong();
		}
		return new Var(addedValue);
	}

	@CronapiMetaData(type = "function", name = "{{addDoubleName}}", nameTags = {
			"addDouble" }, description = "{{addDoubleDescription}}", params = { "{{addDoubleParam0}}" }, paramsType = {
					ObjectType.OBJECT }, returnType = ObjectType.DOUBLE, arbitraryParams = true)
	public static final Var addDouble(Var... values) throws Exception {
		Double addedValue = 0.0;
		for (Var value : values) {
			addedValue += value.getObjectAsDouble();
		}
		return new Var(addedValue);
	}

	@CronapiMetaData(type = "function", name = "{{subtractLongName}}", nameTags = {
			"subtractLong" }, description = "{{subtractLongDescription}}", params = {
					"{{subtractLongParam0}}" }, paramsType = {
							ObjectType.OBJECT }, returnType = ObjectType.LONG, arbitraryParams = true)
	public static final Var subtractLong(Var... values) throws Exception {
		Long initialValue = values[0].getObjectAsLong();
		for (int i = 1; i < values.length; i++) {
			Var value = values[i];
			initialValue -= value.getObjectAsLong();
		}
		return new Var(initialValue);
	}

	@CronapiMetaData(type = "function", name = "{{subtractDoubleName}}", nameTags = {
			"subtractDouble" }, description = "{{subtractDoubleDescription}}", params = {
					"{{subtractDoubleParam0}}" }, paramsType = {
							ObjectType.OBJECT }, returnType = ObjectType.DOUBLE, arbitraryParams = true)
	public static final Var subtractDouble(Var... values) throws Exception {
		Double initialValue = values[0].getObjectAsDouble();
		for (int i = 1; i < values.length; i++) {
			Var value = values[i];
			initialValue -= value.getObjectAsDouble();
		}
		return new Var(initialValue);
	}

	@CronapiMetaData(type = "function", name = "{{multiplyLongName}}", nameTags = {
			"multiplyLong" }, description = "{{multiplyLongDescription}}", params = {
					"{{multiplyLongParam0}}" }, paramsType = {
							ObjectType.OBJECT }, returnType = ObjectType.LONG, arbitraryParams = true)
	public static final Var multiplyLong(Var... values) throws Exception {
		Long returnValue = 1L;
		for (Var value : values) {
			returnValue *= value.getObjectAsLong();
		}
		return new Var(returnValue);
	}

	@CronapiMetaData(type = "function", name = "{{multiplyDoubleName}}", nameTags = {
			"multiplyDouble" }, description = "{{multiplyDoubleDescription}}", params = {
					"{{multiplyDoubleParam0}}" }, paramsType = {
							ObjectType.OBJECT }, returnType = ObjectType.DOUBLE, arbitraryParams = true)
	public static final Var multiplyDouble(Var... values) throws Exception {
		Double returnValue = 1.0;
		for (Var value : values) {
			returnValue *= value.getObjectAsDouble();
		}
		return new Var(returnValue);
	}
}
