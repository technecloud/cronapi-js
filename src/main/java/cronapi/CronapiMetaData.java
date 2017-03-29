package cronapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
public @interface CronapiMetaData {
     String type() default "";
     String category() default "";
     String[] categorySynonymous() default "";
     String name() default "";
     String[] nameSynonymous() default "";
     String description() default "";
     String[] params() default "";
}