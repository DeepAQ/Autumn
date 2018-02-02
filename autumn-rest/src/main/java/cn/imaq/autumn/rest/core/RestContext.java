package cn.imaq.autumn.rest.core;

import cn.imaq.autumn.rest.annotation.RequestMapping;
import cn.imaq.autumn.rest.annotation.RestController;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RestContext {
    @Getter
    private List<RequestMappingModel> mappings = new ArrayList<>();

    private RestContext() {
    }

    public static RestContext build() {
        log.info("Scanning for controllers ...");
        RestContext context = new RestContext();
        new FastClasspathScanner().matchClassesWithAnnotation(RestController.class, cls -> {
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
                    context.getMappings().add(mapping);
                    log.info("Mapped " + mapping.getPaths() + " to " + method);
                }
            }
        }).scan();
        return context;
    }
}
