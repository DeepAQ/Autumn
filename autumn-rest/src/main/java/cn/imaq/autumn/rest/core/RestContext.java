package cn.imaq.autumn.rest.core;

import cn.imaq.autumn.core.context.AutumnContext;
import cn.imaq.autumn.rest.model.ExceptionHandlerModel;
import cn.imaq.autumn.rest.model.RequestMappingModel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RestContext {
    public static final String ATTR = RestContext.class.getName();

    @Getter
    private AutumnContext applicationContext;
    private List<RequestMappingModel> mappings = new ArrayList<>();
    private List<ExceptionHandlerModel> exceptionHandlers = new ArrayList<>();
    private Map<Class<?>, Object> instances = new ConcurrentHashMap<>();

    private RestContext(AutumnContext applicationContext) {
        this.applicationContext = applicationContext;
        applicationContext.setAttribute(ATTR, this);
    }

    public static RestContext build() {
        AutumnContext applicationContext = new AutumnContext("applicationContext");
        RestContext restContext = new RestContext(applicationContext);
        applicationContext.scanComponents();
        return restContext;
    }

    @SuppressWarnings("unchecked")
    public <T> T getInstance(Class<? extends T> clazz) {
        Object instance = instances.get(clazz);
        if (instance != null && clazz.isInstance(instance)) {
            return (T) instance;
        }
        try {
            instance = clazz.newInstance();
            instances.put(clazz, instance);
            return (T) instance;
        } catch (InstantiationException | IllegalAccessException e) {
            log.error("Error instantiating {}: {}", clazz.getName(), String.valueOf(e));
            return null;
        }
    }

    public void addMapping(RequestMappingModel mappingModel) {
        this.mappings.add(mappingModel);
    }

    public void addExceptionHandler(ExceptionHandlerModel exceptionHandlerModel) {
        this.exceptionHandlers.add(exceptionHandlerModel);
    }

    public RequestMappingModel matchRequestMapping(HttpServletRequest req) {
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

    public ExceptionHandlerModel matchExceptionHandler(Class<?> controllerClass, Class<? extends Throwable> throwableClass) {
        for (ExceptionHandlerModel handler : exceptionHandlers) {
            if (handler.getControllerAdvice().matches(controllerClass)) {
                for (Class<? extends Throwable> thrClass : handler.getThrowableClasses()) {
                    if (thrClass.isAssignableFrom(throwableClass)) {
                        return handler;
                    }
                }
            }
        }
        return null;
    }
}
