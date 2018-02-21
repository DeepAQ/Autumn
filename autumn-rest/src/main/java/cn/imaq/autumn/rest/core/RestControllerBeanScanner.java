package cn.imaq.autumn.rest.core;

import cn.imaq.autumn.core.beans.BeanInfo;
import cn.imaq.autumn.core.beans.creator.NormalBeanCreator;
import cn.imaq.autumn.core.beans.scanner.BeanScanner;
import cn.imaq.autumn.core.context.AutumnContext;
import cn.imaq.autumn.rest.annotation.RequestMapping;
import cn.imaq.autumn.rest.annotation.RestController;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanSpec;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

@Slf4j
public class RestControllerBeanScanner implements BeanScanner {
    @Override
    public void process(ScanSpec spec, AutumnContext context) {
        RestContext restContext = (RestContext) context.getAttribute(RestContext.ATTR);
        if (restContext != null) {
            spec.matchClassesWithAnnotation(RestController.class, cls -> {
                log.info("Found controller " + cls.getName());
                RequestMappingModel parentMapping = null;
                if (cls.isAnnotationPresent(RequestMapping.class)) {
                    parentMapping = RequestMappingModel.fromAnnotation(cls.getAnnotation(RequestMapping.class));
                }
                for (Method method : cls.getMethods()) {
                    if (method.isAnnotationPresent(RequestMapping.class)) {
                        RequestMappingModel mapping = RequestMappingModel.fromAnnotation(method.getAnnotation(RequestMapping.class));
                        if (parentMapping != null) {
                            mapping.combine(parentMapping);
                        }
                        mapping.setMethod(method);
                        restContext.addMapping(mapping);
                        log.info("Mapped " + mapping.getPaths() + " to " + method);
                    }
                }
                context.addBeanInfo(BeanInfo.builder()
                        .type(cls)
                        .singleton(true)
                        .creator(new NormalBeanCreator(cls))
                        .build());
            });
        }
    }
}
