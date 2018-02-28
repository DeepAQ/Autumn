package cn.imaq.autumn.rest.scanner;

import cn.imaq.autumn.core.beans.BeanInfo;
import cn.imaq.autumn.core.beans.creator.NormalBeanCreator;
import cn.imaq.autumn.core.beans.scanner.BeanScanner;
import cn.imaq.autumn.core.context.AutumnContext;
import cn.imaq.autumn.rest.annotation.ControllerAdvice;
import cn.imaq.autumn.rest.annotation.ExceptionHandler;
import cn.imaq.autumn.rest.core.RestContext;
import cn.imaq.autumn.rest.model.ControllerAdviceModel;
import cn.imaq.autumn.rest.model.ExceptionHandlerModel;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanSpec;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

@Slf4j
public class ControllerAdviceBeanScanner implements BeanScanner {
    @Override
    public void process(ScanSpec spec, AutumnContext context) {
        RestContext restContext = (RestContext) context.getAttribute(RestContext.ATTR);
        if (restContext != null) {
            spec.matchClassesWithAnnotation(ControllerAdvice.class, cls -> {
                log.info("Found controller advice {}", cls.getName());
                context.addBeanInfo(BeanInfo.builder()
                        .name(cls.getSimpleName().toLowerCase())
                        .type(cls)
                        .singleton(true)
                        .creator(new NormalBeanCreator(cls))
                        .build());

                ControllerAdvice ca = cls.getAnnotation(ControllerAdvice.class);
                ControllerAdviceModel caModel = new ControllerAdviceModel(ca);
                for (Method method : cls.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(ExceptionHandler.class)) {
                        method.setAccessible(true);
                        ExceptionHandler eh = method.getAnnotation(ExceptionHandler.class);
                        restContext.addExceptionHandler(new ExceptionHandlerModel(eh, caModel, method));
                    }
                }
            });
        }
    }
}
