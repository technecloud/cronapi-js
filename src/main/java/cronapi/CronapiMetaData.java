package cronapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value={ElementType.METHOD, ElementType.TYPE})
public @interface CronapiMetaData {
	String type() default "";

	CategoryType category() default CategoryType.OTHER;

	String[] categoryTags() default "";

	String name() default "";

	String[] nameTags() default "";

	String description() default "";

	String[] params() default "";
	
	boolean arbitraryParams()  default false;

	ObjectType[] paramsType() default { ObjectType.UNKNOWN };

	ObjectType returnType() default ObjectType.VOID;

	public enum CategoryType {
		CONVERSION, IO, UTIL, XML, DATETIME, EMAIL, FTP, OTHER
	}

	public enum ObjectType {
		BOOLEAN, LONG, DOUBLE, DATETIME, STRING, LIST, MAP, DATASET, JSON, XML, OBJECT, UNKNOWN, VOID
	}
}