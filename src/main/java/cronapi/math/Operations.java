package cronapi.math;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
		case INT: {
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
		case INT: {
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
	public static final Var sum(Var... values) throws Exception {
		Var result = new Var();

		switch (values[0].getType()) {
		case DOUBLE: {
			result = addDouble(values);
			break;
		}
		case INT: {
			result = addLong(values);
			break;
		}
		default: {
			result = addDouble(values);
		}

		}
		return result;
	}

	@CronapiMetaData(type = "function", name = "{{addFunctionName}}", nameTags = {
			"addFunction" }, description = "{{addFunctionDescription}}", params = {
					"{{addFunctionParam0}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.OBJECT)
	public static final Var listSum(Var values) throws Exception {
		return new Var(sum(values));
	}

	@CronapiMetaData(type = "function", name = "{{addLongName}}", nameTags = {
			"addLong" }, description = "{{addLongDescription}}", params = { "{{addLongParam0}}" }, paramsType = {
					ObjectType.OBJECT }, returnType = ObjectType.LONG, arbitraryParams = true)
	public static final Var addLong(Var... values) throws Exception {
		Long addedValue = 0L;
		for (Var value : values) {
			if (value.getType() == Var.Type.LIST) {

				for (Var v : value.getObjectAsList()) {
					addedValue += v.getObjectAsLong();
				}
			} else
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

			if (value.getType() == Var.Type.LIST) {

				for (Var v : value.getObjectAsList()) {
					addedValue += v.getObjectAsDouble();
				}
			} else
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

	@CronapiMetaData(type = "function", name = "{{absFunctionName}}", nameTags = {
			"absFunction" }, description = "{{absFunctionDescription}}", params = {
					"{{absFunctionParam0}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.OBJECT)
	public static final Var abs(Var value) throws Exception {
		Var result;
		switch (value.getType()) {
		case DOUBLE: {
			result = new Var(Math.abs(value.getObjectAsDouble()));
			break;
		}
		case INT: {
			result = new Var(Math.abs(value.getObjectAsLong()));
			break;
		}
		default: {
			result = new Var(Math.abs(value.getObjectAsDouble()));
		}
		}
		return result;
	}

	@CronapiMetaData(type = "function", name = "{{rootFunctionName}}", nameTags = {
			"rootFunction" }, description = "{{rootFunctionDescription}}", params = {
					"{{rootFunctionParam0}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.OBJECT)
	public static final Var root(Var value) throws Exception {
		Var result;
		switch (value.getType()) {
		case DOUBLE: {
			result = new Var(Math.sqrt(value.getObjectAsDouble()));
			break;
		}
		case INT: {
			result = new Var(Math.sqrt(value.getObjectAsLong()));
			break;
		}
		default: {
			result = new Var(Math.sqrt(value.getObjectAsInt()));
		}
		}
		return result;
	}

	@CronapiMetaData(type = "function", name = "{{logFunctionName}}", nameTags = {
			"logFunction" }, description = "{{logFunctionDescription}}", params = {
					"{{logFunctionParam0}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.OBJECT)
	public static final Var log(Var value) throws Exception {
		Var result;
		switch (value.getType()) {
		case DOUBLE: {
			result = new Var(Math.log(value.getObjectAsDouble()));
			break;
		}
		case INT: {
			result = new Var(Math.log(value.getObjectAsLong()));
			break;
		}
		default: {
			result = new Var(Math.log(value.getObjectAsInt()));
		}
		}
		return result;
	}

	@CronapiMetaData(type = "function", name = "{{log10FunctionName}}", nameTags = {
			"log10Function" }, description = "{{log10FunctionDescription}}", params = {
					"{{log10FunctionParam0}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.OBJECT)
	public static final Var log10(Var value) throws Exception {
		Var result;
		switch (value.getType()) {
		case DOUBLE: {
			result = new Var(Math.log10(value.getObjectAsDouble()));
			break;
		}
		case INT: {
			result = new Var(Math.log10(value.getObjectAsLong()));
			break;
		}
		default: {
			result = new Var(Math.log10(value.getObjectAsInt()));
		}
		}
		return result;
	}

	@CronapiMetaData(type = "function", name = "{{expFunctionName}}", nameTags = {
			"expFunction" }, description = "{{expFunctionDescription}}", params = {
					"{{expFunctionParam0}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.OBJECT)
	public static final Var exp(Var value) throws Exception {
		Var result;
		switch (value.getType()) {
		case DOUBLE: {
			result = new Var(Math.exp(value.getObjectAsDouble()));
			break;
		}
		case INT: {
			result = new Var(Math.exp(value.getObjectAsLong()));
			break;
		}
		default: {
			result = new Var(Math.exp(value.getObjectAsInt()));
		}
		}
		return result;
	}

	@CronapiMetaData(type = "function", name = "{{powFunctionName}}", nameTags = {
			"powFunction" }, description = "{{powFunctionDescription}}", params = { "{{powFunctionParam0}}",
					"{{powFunctionParam1}}" }, paramsType = { ObjectType.OBJECT,
							ObjectType.OBJECT }, returnType = ObjectType.OBJECT)
	public static final Var pow(Var value1, Var value2) throws Exception {
		Var result;
		switch (value1.getType()) {
		case DOUBLE: {
			result = new Var(Math.pow(value1.getObjectAsDouble(), value2.getObjectAsDouble()));
			break;
		}
		case INT: {
			result = new Var(Math.pow(value1.getObjectAsLong(), value2.getObjectAsLong()));
			break;
		}
		default: {
			result = new Var(Math.pow(value1.getObjectAsInt(), value2.getObjectAsInt()));
		}
		}
		return result;
	}

	@CronapiMetaData(type = "function", name = "{{pow10FunctionName}}", nameTags = {
			"pow10Function" }, description = "{{pow10FunctionDescription}}", params = {
					"{{pow10FunctionParam0}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.OBJECT)
	public static final Var pow10(Var value1) throws Exception {
		Var result;
		switch (value1.getType()) {
		case DOUBLE: {
			result = new Var(Math.pow(10, value1.getObjectAsDouble()));
			break;
		}
		case INT: {
			result = new Var(Math.pow(10, value1.getObjectAsLong()));
			break;
		}
		default: {
			result = new Var(Math.pow(10, value1.getObjectAsDouble()));
		}
		}
		return result;
	}

	@CronapiMetaData(type = "function", name = "{{roundFunctionName}}", nameTags = {
			"roundFunction" }, description = "{{roundFunctionDescription}}", params = {
					"{{roundFunctionParam0}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.OBJECT)
	public static final Var round(Var value) throws Exception {
		Var result;
		switch (value.getType()) {
		case DOUBLE: {
			result = new Var(Math.round(value.getObjectAsDouble()));
			break;
		}
		case INT: {
			result = new Var(Math.round(value.getObjectAsLong()));
			break;
		}
		default: {
			result = new Var(Math.round(value.getObjectAsInt()));
		}
		}
		return result;
	}

	@CronapiMetaData(type = "function", name = "{{ceilFunctionName}}", nameTags = {
			"ceilFunction" }, description = "{{ceilFunctionDescription}}", params = {
					"{{ceilFunctionParam0}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.OBJECT)
	public static final Var ceil(Var value) throws Exception {
		Var result;
		switch (value.getType()) {
		case DOUBLE: {
			result = new Var(Math.ceil(value.getObjectAsDouble()));
			break;
		}
		case INT: {
			result = new Var(Math.ceil(value.getObjectAsLong()));
			break;
		}
		default: {
			result = new Var(Math.ceil(value.getObjectAsInt()));
		}
		}
		return result;
	}

	@CronapiMetaData(type = "function", name = "{{floorFunctionName}}", nameTags = {
			"floorFunction" }, description = "{{floorFunctionDescription}}", params = {
					"{{floorFunctionParam0}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.OBJECT)
	public static final Var floor(Var value) throws Exception {
		Var result;
		switch (value.getType()) {
		case DOUBLE: {
			result = new Var(Math.floor(value.getObjectAsDouble()));
			break;
		}
		case INT: {
			result = new Var(Math.floor(value.getObjectAsLong()));
			break;
		}
		default: {
			result = new Var(Math.floor(value.getObjectAsInt()));
		}
		}
		return result;
	}

	@CronapiMetaData(type = "function", name = "{{sinFunctionName}}", nameTags = {
			"sinFunction" }, description = "{{sinFunctionDescription}}", params = {
					"{{sinFunctionParam0}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.OBJECT)
	public static final Var sin(Var value) throws Exception {
		Var result;
		switch (value.getType()) {
		case DOUBLE: {
			result = new Var(Math.sin(value.getObjectAsDouble()));
			break;
		}
		case INT: {
			result = new Var(Math.sin(value.getObjectAsLong()));
			break;
		}
		default: {
			result = new Var(Math.sin(value.getObjectAsInt()));
		}
		}
		return result;
	}

	@CronapiMetaData(type = "function", name = "{{cosFunctionName}}", nameTags = {
			"cosFunction" }, description = "{{cosFunctionDescription}}", params = {
					"{{cosFunctionParam0}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.OBJECT)
	public static final Var cos(Var value) throws Exception {
		Var result;
		switch (value.getType()) {
		case DOUBLE: {
			result = new Var(Math.cos(value.getObjectAsDouble()));
			break;
		}
		case INT: {
			result = new Var(Math.cos(value.getObjectAsLong()));
			break;
		}
		default: {
			result = new Var(Math.cos(value.getObjectAsInt()));
		}
		}
		return result;
	}

	@CronapiMetaData(type = "function", name = "{{tanFunctionName}}", nameTags = {
			"tanFunction" }, description = "{{tanFunctionDescription}}", params = {
					"{{tanFunctionParam0}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.OBJECT)
	public static final Var tan(Var value) throws Exception {
		Var result;
		switch (value.getType()) {
		case DOUBLE: {
			result = new Var(Math.tan(value.getObjectAsDouble()));
			break;
		}
		case INT: {
			result = new Var(Math.tan(value.getObjectAsLong()));
			break;
		}
		default: {
			result = new Var(Math.tan(value.getObjectAsInt()));
		}
		}
		return result;
	}

	@CronapiMetaData(type = "function", name = "{{asinFunctionName}}", nameTags = {
			"asinFunction" }, description = "{{asinFunctionDescription}}", params = {
					"{{asinFunctionParam0}}" }, paramsType = { ObjectType.DOUBLE }, returnType = ObjectType.DOUBLE)
	public static final Var asin(Var value) throws Exception {
		Var result;
		if (value.getObjectAsDouble() > 1.0)
			return new Var(Math.acos(1));

		switch (value.getType()) {
		case DOUBLE: {
			result = new Var(Math.asin(value.getObjectAsDouble()));
			break;
		}
		case INT: {
			result = new Var(Math.asin(value.getObjectAsLong()));
			break;
		}
		default: {
			result = new Var(Math.asin(value.getObjectAsInt()));
		}
		}
		return result;
	}

	@CronapiMetaData(type = "function", name = "{{acosFunctionName}}", nameTags = {
			"acosFunction" }, description = "{{acosFunctionDescription}}", params = {
					"{{acosFunctionParam0}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.OBJECT)
	public static final Var acos(Var value) throws Exception {
		Var result;
		if (value.getObjectAsDouble() > 1.0)
			return new Var(Math.acos(1));

		switch (value.getType()) {
		case DOUBLE: {
			result = new Var(Math.acos(value.getObjectAsDouble()));
			break;
		}
		case INT: {
			result = new Var(Math.acos(value.getObjectAsLong()));
			break;
		}
		default: {
			result = new Var(Math.acos(value.getObjectAsInt()));
		}
		}
		return result;
	}

	@CronapiMetaData(type = "function", name = "{{atanFunctionName}}", nameTags = {
			"atanFunction" }, description = "{{atanFunctionDescription}}", params = {
					"{{atanFunctionParam0}}" }, paramsType = { ObjectType.DOUBLE }, returnType = ObjectType.DOUBLE)
	public static final Var atan(Var value) throws Exception {

		Var result;
		switch (value.getType()) {
		case DOUBLE: {
			result = new Var(Math.atan(value.getObjectAsDouble()));
			break;
		}
		case INT: {
			result = new Var(Math.atan(value.getObjectAsLong()));
			break;
		}
		default: {
			result = new Var(Math.atan(value.getObjectAsInt()));
		}
		}
		return result;
	}

	@CronapiMetaData(type = "function", name = "{{PIFunctionName}}", nameTags = {
			"PIFunction" }, description = "{{PIFunctionDescription}}", params = {
					"{{PIFunctionParam0}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.OBJECT)
	public static final Var pi() throws Exception {
		return new Var(Math.PI);
	}

	@CronapiMetaData(type = "function", name = "{{negFunctionName}}", nameTags = {
			"negFunction" }, description = "{{negFunctionDescription}}", params = {
					"{{negFunctionParam0}}" }, paramsType = { ObjectType.DOUBLE }, returnType = ObjectType.DOUBLE)
	public static final Var neg(Var value) throws Exception {

		Var result;
		switch (value.getType()) {
		case DOUBLE: {
			result = new Var(value.getObjectAsDouble() * -1);
			break;
		}
		case INT: {
			result = new Var(value.getObjectAsLong() * -1);
			break;
		}
		default: {
			result = new Var(value.getObjectAsLong() * -1);
		}
		}
		return result;
	}

	@CronapiMetaData(type = "function", name = "{{infinityFunctionName}}", nameTags = {
			"infinityFunction" }, description = "{{infinityFunctionDescription}}", returnType = ObjectType.DOUBLE)
	public static final Var infinity() throws Exception {
		return new Var(Double.POSITIVE_INFINITY);
	}

	@CronapiMetaData(type = "function", name = "{{MathEFunctionName}}", nameTags = {
			"MathEFunction" }, description = "{{MathEFunctionDescription}}", returnType = ObjectType.DOUBLE)
	public static final Var e() throws Exception {
		return new Var(Math.E);
	}

	@CronapiMetaData(type = "function", name = "{{goldenRatioFunctionName}}", nameTags = {
			"goldenRatioFunction" }, description = "{{goldenRatioFunctionDescription}}", returnType = ObjectType.DOUBLE)
	public static final Var goldenRatio() throws Exception {
		return new Var((1 + Math.sqrt(5)) / 2);
	}

	@CronapiMetaData(type = "function", name = "{{isEvenFunctionName}}", nameTags = {
			"isEvenRatioFunction" }, description = "{{isEvenRatioFunctionDescription}}", params = {
					"{{isEvenFunctionParam0}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.DOUBLE)
	public static final Var isEven(Var value) throws Exception {
		if (value.getObjectAsInt() % 2 == 0)
			return new Var(true);
		return new Var(false);
	}

	@CronapiMetaData(type = "function", name = "{{isOddFunctionName}}", nameTags = {
			"isOddRatioFunction" }, description = "{{isOddRatioFunctionDescription}}", params = {
					"{{isOddFunctionParam0}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.DOUBLE)
	public static final Var isOdd(Var value) throws Exception {
		if (value.getObjectAsInt() % 2 == 1)
			return new Var(true);
		return new Var(false);
	}

	@CronapiMetaData(type = "function", name = "{{isPrimeFunctionName}}", nameTags = {
			"isPrimeRatioFunction" }, description = "{{isPrimeRatioFunctionDescription}}", params = {
					"{{isPrimeFunctionParam0}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.DOUBLE)
	public static final Var isPrime(Var value) throws Exception {

		if (value.getObjectAsLong() < 2)
			return new Var(false);
		if (value.getObjectAsLong() == 2)
			return new Var(true);
		if (value.getObjectAsLong() % 2 == 0)
			return new Var(false);
		for (int i = 3; i * i <= value.getObjectAsLong(); i += 2)
			if (value.getObjectAsLong() % i == 0)
				return new Var(false);
		return new Var(true);
	}

	@CronapiMetaData(type = "function", name = "{{isIntFunctionName}}", nameTags = {
			"isIntRatioFunction" }, description = "{{isIntFunctionDescription}}", params = {
					"{{isIntFunctionParam0}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.DOUBLE)
	public static final Var isInt(Var value) throws Exception {
		if (value.getType() == Var.Type.INT)
			return new Var(true);
		return new Var(false);
	}

	@CronapiMetaData(type = "function", name = "{{isPositiveFunctionName}}", nameTags = {
			"isPositiveRatioFunction" }, description = "{{isPositiveFunctionDescription}}", params = {
					"{{isPositiveFunctionParam0}}" }, paramsType = {
							ObjectType.OBJECT }, returnType = ObjectType.DOUBLE)
	public static final Var isPositive(Var value) throws Exception {
		if (value.getObjectAsLong() >= 0)
			return new Var(true);
		return new Var(false);
	}

	@CronapiMetaData(type = "function", name = "{{isNegativeFunctionName}}", nameTags = {
			"isNegativeRatioFunction" }, description = "{{isNegativeRatioFunctionDescription}}", params = {
					"{{isNegativeFunctionParam0}}" }, paramsType = {
							ObjectType.OBJECT }, returnType = ObjectType.DOUBLE)
	public static final Var isNegative(Var value) throws Exception {
		if (value.getObjectAsLong() < 0)
			return new Var(true);
		return new Var(false);
	}

	@CronapiMetaData(type = "function", name = "{{isDivisibleByFunctionName}}", nameTags = {
			"isDivisibleByFunction" }, description = "{{isDivisibleByFunctionDescription}}", params = {
					"{{isDivisibleByFunctionParam0}}" }, paramsType = {
							ObjectType.OBJECT }, returnType = ObjectType.DOUBLE)
	public static final Var isDivisibleBy(Var value1, Var value2) throws Exception {

		if ((value1.getObjectAsDouble() % value2.getObjectAsDouble()) == 0)
			return new Var(true);
		return new Var(false);
	}

	@CronapiMetaData(type = "function", name = "{{isDivisibleByFunctionName}}", nameTags = {
			"isDivisibleByFunction" }, description = "{{isDivisibleByFunctionDescription}}", params = {
					"{{isDivisibleByFunctionParam0}}" }, paramsType = { ObjectType.LONG,
							ObjectType.LONG }, returnType = ObjectType.LONG)
	public static final Var randomInt(Var min, Var max) throws Exception {
		Var result;
		Random random = new Random();
		switch (min.getType()) {
		case DOUBLE: {
			int resultado = random.nextInt(max.getObjectAsInt());
			while ((resultado < min.getObjectAsLong()) || (resultado > max.getObjectAsLong())) {
				resultado = random.nextInt();
			}
			result = new Var(resultado);
			break;
		}
		case INT: {
			int resultado = random.nextInt();
			while (resultado < min.getObjectAsLong() || resultado > max.getObjectAsLong()) {
				resultado = random.nextInt();
			}
			result = new Var(resultado);
			break;
		}
		default: {
			int resultado = random.nextInt();
			while (resultado < min.getObjectAsLong() || resultado > max.getObjectAsLong()) {
				resultado = random.nextInt();
			}
			result = new Var(resultado);
		}
		}
		return result;
	}

	@CronapiMetaData(type = "function", name = "{{isDivisibleByFunctionName}}", nameTags = {
			"isDivisibleByFunction" }, description = "{{isDivisibleByFunctionDescription}}", params = {
					"{{isDivisibleByFunctionParam0}}" }, paramsType = {
							ObjectType.OBJECT }, returnType = ObjectType.DOUBLE)
	public static final Var randomFloat() throws Exception {
		Random random = new Random();
		double result = random.nextGaussian();
		while (result < 0.0 || result > 1.0)
			result = random.nextGaussian();
		return new Var(result);
	}

	@CronapiMetaData(type = "function", name = "{{isDivisibleByFunctionName}}", nameTags = {
			"isDivisibleByFunction" }, description = "{{isDivisibleByFunctionDescription}}", params = {
					"{{isDivisibleByFunctionParam0}}" }, paramsType = {
							ObjectType.LIST }, returnType = ObjectType.DOUBLE)
	public static final Var listSmaller(Var value) throws Exception {

		Var result;
		switch (value.getObjectAsList().getFirst().getType()) {
		case DOUBLE: {
			result = new Var(value.getObjectAsList().getFirst().getObjectAsDouble());
			for (Var v : value.getObjectAsList()) {
				if (v.getObjectAsDouble() < result.getObjectAsDouble())
					result = v;
			}
			break;
		}
		case INT: {
			result = new Var(value.getObjectAsList().getFirst().getObjectAsLong());
			for (Var v : value.getObjectAsList()) {
				if (v.getObjectAsLong() < result.getObjectAsLong())
					result = v;
			}
			break;
		}
		default: {
			result = new Var(value.getObjectAsList().getFirst().getObjectAsLong());
			for (Var v : value.getObjectAsList()) {
				if (v.getObjectAsLong() < result.getObjectAsLong())
					result = v;
			}
			break;
		}
		}
		return result;

	}

	@CronapiMetaData(type = "function", name = "{{isDivisibleByFunctionName}}", nameTags = {
			"isDivisibleByFunction" }, description = "{{isDivisibleByFunctionDescription}}", params = {
					"{{isDivisibleByFunctionParam0}}" }, paramsType = {
							ObjectType.LIST }, returnType = ObjectType.DOUBLE)
	public static final Var listLarger(Var value) throws Exception {

		Var result;
		switch (value.getObjectAsList().getFirst().getType()) {
		case DOUBLE: {
			result = new Var(value.getObjectAsList().getFirst().getObjectAsDouble());
			for (Var v : value.getObjectAsList()) {
				if (v.getObjectAsDouble() > result.getObjectAsDouble())
					result = v;
			}
			break;
		}
		case INT: {
			result = new Var(value.getObjectAsList().getFirst().getObjectAsLong());
			for (Var v : value.getObjectAsList()) {
				if (v.getObjectAsLong() > result.getObjectAsLong())
					result = v;
			}
			break;
		}
		default: {
			result = new Var(value.getObjectAsList().getFirst().getObjectAsLong());
			for (Var v : value.getObjectAsList()) {
				if (v.getObjectAsLong() > result.getObjectAsLong())
					result = v;
			}
			break;
		}
		}
		return result;

	}

	@CronapiMetaData(type = "function", name = "{{isDivisibleByFunctionName}}", nameTags = {
			"isDivisibleByFunction" }, description = "{{isDivisibleByFunctionDescription}}", params = {
					"{{isDivisibleByFunctionParam0}}" }, paramsType = {
							ObjectType.LIST }, returnType = ObjectType.DOUBLE)
	public static final Var listAverage(Var value) throws Exception {

		Var result;
		switch (value.getObjectAsList().getFirst().getType()) {
		case DOUBLE: {
			Double sum = 0.0;
			for (Var v : value.getObjectAsList()) {
				sum += v.getObjectAsDouble();
			}
			result = new Var(sum / value.size());
			break;
		}
		case INT: {
			Long sum = 0L;
			for (Var v : value.getObjectAsList()) {
				sum += v.getObjectAsLong();
			}
			result = new Var(sum / value.size());
			break;
		}
		default: {
			Long sum = 0L;
			for (Var v : value.getObjectAsList()) {
				sum += v.getObjectAsLong();
			}
			result = new Var(sum / value.size());
			break;
		}
		}
		return result;

	}

	@CronapiMetaData(type = "function", name = "{{isDivisibleByFunctionName}}", nameTags = {
			"isDivisibleByFunction" }, description = "{{isDivisibleByFunctionDescription}}", params = {
					"{{isDivisibleByFunctionParam0}}" }, paramsType = {
							ObjectType.LIST }, returnType = ObjectType.DOUBLE)
	public static final Var listMedium(Var value) throws Exception {

		switch (value.getObjectAsList().getFirst().getType()) {
		case DOUBLE: {

			LinkedList<Var> lklist = value.getObjectAsList();
			Collections.sort(lklist, new Comparator<Var>() {
				@Override
				public int compare(Var o1, Var o2) {
					if (o1.getObjectAsDouble() > o2.getObjectAsDouble())
						return 1;
					if (o1.getObjectAsDouble() == o2.getObjectAsDouble())
						return 0;
					if (o1.getObjectAsDouble() < o2.getObjectAsDouble())
						return -1;
					return 0;
				}
			});

			if (lklist.size() % 2 == 1)
				return new Var(lklist.get(lklist.size() / 2));
			else {
				Var result = new Var(lklist.get(lklist.size() / 2 - 1));
				result = new Var(
						(result.getObjectAsDouble() + lklist.get(lklist.size() / 2 + 1).getObjectAsDouble()) / 2);
				return result;
			}

		}
		case INT: {

			LinkedList<Var> lklist = value.getObjectAsList();
			Collections.sort(lklist, new Comparator<Var>() {

				@Override
				public int compare(Var o1, Var o2) {
					if (o1.getObjectAsLong() > o2.getObjectAsLong())
						return 1;
					if (o1.getObjectAsLong() == o2.getObjectAsLong())
						return 0;
					if (o1.getObjectAsLong() < o2.getObjectAsLong())
						return -1;
					return 0;
				}
			});

			if (lklist.size() % 2 == 1)
				return new Var(lklist.get(lklist.size() / 2));
			else {
				Var result = new Var(lklist.get(lklist.size() / 2 - 1));
				result = new Var((result.getObjectAsLong() + lklist.get(lklist.size() / 2 + 1).getObjectAsLong()) / 2);
				return result;
			}

		}
		default: {
			LinkedList<Var> lklist = value.getObjectAsList();
			Collections.sort(lklist, new Comparator<Var>() {
				@Override
				public int compare(Var o1, Var o2) {
					if (o1.getObjectAsLong() > o2.getObjectAsLong())
						return 1;
					if (o1.getObjectAsLong() == o2.getObjectAsLong())
						return 0;
					if (o1.getObjectAsLong() < o2.getObjectAsLong())
						return -1;
					return 0;
				}
			});

			if (lklist.size() % 2 == 1)
				return new Var(lklist.get(lklist.size() / 2));
			else {
				Var result = new Var(lklist.get(lklist.size() / 2 - 1));
				result = new Var((result.getObjectAsLong() + lklist.get(lklist.size() / 2 + 1).getObjectAsLong()) / 2);
				return result;
			}

		}
		}
	}

	@CronapiMetaData(type = "function", name = "{{isDivisibleByFunctionName}}", nameTags = {
			"isDivisibleByFunction" }, description = "{{isDivisibleByFunctionDescription}}", params = {
					"{{isDivisibleByFunctionParam0}}" }, paramsType = {
							ObjectType.LIST }, returnType = ObjectType.DOUBLE)
	public static final Var listModes(Var value) throws Exception {
		Var modes = new Var();
		Map<Double, Double> countMap = new HashMap<Double, Double>();
		double max = -1;
		double d;
		LinkedList<Var> ll = value.getObjectAsList();
		for (Var var : ll) {
			d = var.getObjectAsDouble();
			double count = 0;
			if (countMap.containsKey(d)) {
				count = countMap.get(d) + 1;
			} else {
				count = 1;
			}
			countMap.put(d, count);
			if (count > max) {
				max = count;
			}
		}
		for (Map.Entry<Double, Double> tuple : countMap.entrySet()) {
			if (tuple.getValue() == max) {
				modes = new Var(Var.valueOf(tuple.getKey().doubleValue()));
			}
		}
		return modes;
	}

	@CronapiMetaData(type = "function", name = "{{isDivisibleByFunctionName}}", nameTags = {
			"isDivisibleByFunction" }, description = "{{isDivisibleByFunctionDescription}}", params = {
					"{{isDivisibleByFunctionParam0}}" }, paramsType = {
							ObjectType.LIST }, returnType = ObjectType.DOUBLE)
	public static final Var listRandomItem(Var value) throws Exception {
		return new Var(
				value.get(Integer.parseInt("" + randomInt(new Var(0), new Var(value.size())).getObjectAsLong())));

	}

	@CronapiMetaData(type = "function", name = "{{isDivisibleByFunctionName}}", nameTags = {
			"isDivisibleByFunction" }, description = "{{isDivisibleByFunctionDescription}}", params = {
					"{{isDivisibleByFunctionParam0}}" }, paramsType = {
							ObjectType.LIST }, returnType = ObjectType.DOUBLE)
	public static final Var listStandardDeviation(Var value) throws Exception {
		double mean = listMedium(value).getObjectAsDouble();
		double size = value.size();
		double temp = 0;
		double d;
		LinkedList<Var> ll = value.getObjectAsList();
		for (Var var : ll) {
			d = var.getObjectAsDouble();
			temp += (mean - d) * (mean - d);
		}
		double variance = temp / size;
		return new Var(Math.sqrt(variance));

	}

	@CronapiMetaData(type = "function", name = "{{isDivisibleByFunctionName}}", nameTags = {
			"isDivisibleByFunction" }, description = "{{isDivisibleByFunctionDescription}}", params = {
					"{{isDivisibleByFunctionParam0}}" }, paramsType = {
							ObjectType.LIST }, returnType = ObjectType.DOUBLE)
	public static final Var mod(Var value1, Var value2) throws Exception {
		Var result;
		switch (value1.getType()) {
		case DOUBLE: {
			Double resultado = value1.getObjectAsDouble() % value2.getObjectAsDouble();
			return new Var(resultado);
		}
		case INT: {
			Long resultado = value1.getObjectAsLong() % value2.getObjectAsLong();
			return new Var(resultado);
		}
		default: {
			Long resultado = value1.getObjectAsLong() % value2.getObjectAsLong();
			return new Var(resultado);
		}

		}
	}
}
