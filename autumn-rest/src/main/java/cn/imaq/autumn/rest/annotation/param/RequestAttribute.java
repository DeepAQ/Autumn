package cn.imaq.autumn.rest.annotation.param;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Get one attribute from {@link javax.servlet.http.HttpServletRequest}
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestAttribute {
    String value();

    boolean required() default true;

    String defaultValue() default "";
}
