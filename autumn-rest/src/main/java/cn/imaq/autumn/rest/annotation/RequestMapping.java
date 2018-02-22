package cn.imaq.autumn.rest.annotation;

import cn.imaq.autumn.rest.core.RequestMethod;
import cn.imaq.autumn.rest.message.DefaultConverterDelegate;
import cn.imaq.autumn.rest.message.MessageConverter;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RequestMappings.class)
public @interface RequestMapping {
    String[] value();

    RequestMethod[] method() default {};

    String[] consumes() default {};

    String produces() default "text/html";

    Class<? extends MessageConverter> converter() default DefaultConverterDelegate.class;
}
