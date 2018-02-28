package cn.imaq.autumn.rest.annotation;

import cn.imaq.autumn.rest.message.DefaultConverterDelegate;
import cn.imaq.autumn.rest.message.MessageConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExceptionHandler {
    Class<? extends Throwable>[] value() default Exception.class;

    int status() default 500;

    String produces() default "text/html";

    Class<? extends MessageConverter> converter() default DefaultConverterDelegate.class;
}
