package cn.imaq.autumn.rest.scanner;

import cn.imaq.autumn.core.beans.BeanInfo;
import cn.imaq.autumn.core.beans.creator.NormalBeanCreator;
import cn.imaq.autumn.core.beans.scanner.BeanScanner;
import cn.imaq.autumn.core.context.AutumnContext;
import cn.imaq.autumn.rest.annotation.RequestMapping;
import cn.imaq.autumn.rest.annotation.RequestMappings;
import cn.imaq.autumn.rest.annotation.RestController;
import cn.imaq.autumn.rest.core.RequestMappingModel;
import cn.imaq.autumn.rest.core.RestContext;
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
                    if (method.isAnnotationPresent(RequestMappings.class)) {
                        for (RequestMapping mappingAnno : method.getAnnotation(RequestMappings.class).value()) {
                            addMapping(restContext, mappingAnno, parentMapping, method);
                        }
                    } else if (method.isAnnotationPresent(RequestMapping.class)) {
                        addMapping(restContext, method.getAnnotation(RequestMapping.class), parentMapping, method);
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

    private void addMapping(RestContext restContext, RequestMapping mappingAnno, RequestMappingModel parentMapping, Method method) {
        RequestMappingModel mapping = RequestMappingModel.fromAnnotation(mappingAnno);
        if (parentMapping != null) {
            mapping.combine(parentMapping);
        }
        mapping.setMethod(method);
        restContext.addMapping(mapping);
        log.info("Mapped " + mapping.getPaths() + " to " + method);
    }
}
