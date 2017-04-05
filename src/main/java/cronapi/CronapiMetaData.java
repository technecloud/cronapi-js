package cronapi;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CronapiMetaData {
	String type() default "";

	CategoryType category() default CategoryType.OTHER;

	String[] categoryTags() default "";

	String name() default "";

	String[] nameTags() default "";

	String description() default "";

	String[] params() default "";

	ObjectType[] paramsType() default { ObjectType.UNKNOWN };

	ObjectType returnType() default ObjectType.VOID;

	public enum CategoryType {
		CONVERSION, IO, UTIL, XML, OTHER
	}

	public enum ObjectType {
		BOOLEAN, INTEGER, FLOAT, DATETIME, STRING, LIST, MAP, DATASET, JSON, XML, OBJECT, UNKNOWN, VOID
	}
}