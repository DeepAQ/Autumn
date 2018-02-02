package cn.imaq.autumn.rest.annotation;

import cn.imaq.autumn.rest.core.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {
    String[] value();

    RequestMethod[] method() default {};

    String[] consumes() default {};

    String[] produces() default {};
}
