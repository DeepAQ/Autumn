package cn.imaq.autumn.rest.annotation.param;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Get a {@link com.fasterxml.jackson.databind.JsonNode} object from root node or child node of request body
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface JSONBody {
    String value() default "";
}
