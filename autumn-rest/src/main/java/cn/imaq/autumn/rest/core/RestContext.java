package cn.imaq.autumn.rest.core;

import cn.imaq.autumn.rest.annotation.RequestMapping;
import cn.imaq.autumn.rest.annotation.RestController;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RestContext {
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
                    context.mappings.add(mapping);
                    log.info("Mapped " + mapping.getPaths() + " to " + method);
                }
            }
        }).scan();
        return context;
    }

    public RequestMappingModel matchRequest(HttpServletRequest req) {
        String realPath = req.getServletPath();
        String pathInfo = req.getPathInfo();
        if (pathInfo != null) {
            realPath += pathInfo;
        }
        for (RequestMappingModel mapping : mappings) {
            if (mapping.getPaths().contains(realPath)) {
                if (!mapping.getMethods().isEmpty() && !mapping.getMethods().contains(RequestMethod.valueOf(req.getMethod()))) {
                    continue;
                }
                if (!mapping.getConsumes().isEmpty() && !mapping.getConsumes().contains(req.getContentType())) {
                    continue;
                }
                return mapping;
            }
        }
        return null;
    }
}
