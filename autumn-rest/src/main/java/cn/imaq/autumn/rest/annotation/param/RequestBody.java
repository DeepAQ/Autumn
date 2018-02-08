package cn.imaq.autumn.rest.annotation.param;

import cn.imaq.autumn.rest.message.DefaultConverterDelegate;
import cn.imaq.autumn.rest.message.MessageConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Get response body, if param type is neither {@link String} nor {@code byte[]}, converter will be used.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestBody {
    Class<? extends MessageConverter> converter() default DefaultConverterDelegate.class;
}
