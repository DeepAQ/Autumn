package cn.imaq.autumn.rest.annotation;

import cn.imaq.autumn.rest.annotation.meta.AutumnController;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@AutumnController
public @interface RestController {
}
