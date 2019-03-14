package cn.imaq.autumn.rest.scanner;

import cn.imaq.autumn.core.beans.BeanInfo;
import cn.imaq.autumn.core.beans.creator.NormalBeanCreator;
import cn.imaq.autumn.core.beans.scanner.BeanScanner;
import cn.imaq.autumn.core.context.AutumnContext;
import cn.imaq.autumn.cpscan.ScanResult;
import cn.imaq.autumn.rest.annotation.ExceptionHandler;
import cn.imaq.autumn.rest.annotation.RequestMapping;
import cn.imaq.autumn.rest.annotation.RequestMappings;
import cn.imaq.autumn.rest.annotation.RestController;
import cn.imaq.autumn.rest.core.RestContext;
import cn.imaq.autumn.rest.model.ControllerAdviceModel;
import cn.imaq.autumn.rest.model.ExceptionHandlerModel;
import cn.imaq.autumn.rest.model.RequestMappingModel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

@Slf4j
public class RestControllerBeanScanner implements BeanScanner {
    @Override
    public void process(ScanResult result, AutumnContext context) {
        RestContext restContext = (RestContext) context.getAttribute(RestContext.ATTR);
        if (restContext != null) {
            result.getClassesWithAnnotation(RestController.class).forEach(cls -> {
                log.info("Found controller {}", cls.getName());
                RestController anno = cls.getAnnotation(RestController.class);
                String name = anno.value();
                if (name.isEmpty()) {
                    name = cls.getSimpleName().toLowerCase();
                }
                context.addBeanInfo(BeanInfo.builder()
                        .name(name)
                        .type(cls)
                        .singleton(true)
                        .creator(new NormalBeanCreator(cls))
                        .build());

                RequestMappingModel parentMapping = null;
                if (cls.isAnnotationPresent(RequestMapping.class)) {
                    parentMapping = RequestMappingModel.fromAnnotation(cls.getAnnotation(RequestMapping.class));
                }
                for (Method method : cls.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(RequestMappings.class)) {
                        method.setAccessible(true);
                        for (RequestMapping mappingAnno : method.getAnnotation(RequestMappings.class).value()) {
                            addMapping(restContext, mappingAnno, parentMapping, method);
                        }
                    } else if (method.isAnnotationPresent(RequestMapping.class)) {
                        method.setAccessible(true);
                        addMapping(restContext, method.getAnnotation(RequestMapping.class), parentMapping, method);
                    } else if (method.isAnnotationPresent(ExceptionHandler.class)) {
                        method.setAccessible(true);
                        ExceptionHandler eh = method.getAnnotation(ExceptionHandler.class);
                        restContext.addExceptionHandler(new ExceptionHandlerModel(eh, new ControllerAdviceModel(cls.getName()), method));
                    }
                }
            });
        }
    }

    private void addMapping(RestContext restContext, RequestMapping mappingAnno, RequestMappingModel parentMapping, Method method) {
        RequestMappingModel mapping = RequestMappingModel.fromAnnotation(mappingAnno);
        if (parentMapping != null) {
            mapping.combine(parentMapping);
        }
        mapping.setMethod(method);
        restContext.addMapping(mapping);
        log.info("Mapped {} to {}", mapping.getPaths(), method);
    }
}
