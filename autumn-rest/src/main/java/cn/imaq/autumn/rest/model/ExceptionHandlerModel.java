package cn.imaq.autumn.rest.model;

import cn.imaq.autumn.rest.annotation.ExceptionHandler;
import cn.imaq.autumn.rest.message.MessageConverter;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

@Data
public class ExceptionHandlerModel {
    private ControllerAdviceModel controllerAdvice;
    private List<Class<? extends Throwable>> throwableClasses;
    private Method method;
    private int statusCode;
    private String produces;
    private Class<? extends MessageConverter> converter;

    public ExceptionHandlerModel(ExceptionHandler eh, ControllerAdviceModel controllerAdvice, Method method) {
        this.controllerAdvice = controllerAdvice;
        this.method = method;
        this.throwableClasses = Arrays.asList(eh.value());
        this.statusCode = eh.status();
        this.produces = eh.produces();
        this.converter = eh.converter();
    }
}
