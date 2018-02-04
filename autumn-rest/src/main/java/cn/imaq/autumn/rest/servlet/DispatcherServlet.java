package cn.imaq.autumn.rest.servlet;

import cn.imaq.autumn.rest.core.RequestMappingModel;
import cn.imaq.autumn.rest.core.RestContext;
import cn.imaq.autumn.rest.exception.MethodParamResolveException;
import cn.imaq.autumn.rest.param.resolver.MethodParamsResolver;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DispatcherServlet extends HttpServlet {
    private static final String REST_CONTEXT = RestContext.class.getName();

    private RestContext restContext;
    private Map<Class<?>, Object> instances = new ConcurrentHashMap<>();

    @Override
    public void init() throws ServletException {
        ServletContext context = this.getServletContext();
        restContext = (RestContext) context.getAttribute(REST_CONTEXT);
        if (restContext == null) {
            synchronized (context) {
                restContext = (RestContext) context.getAttribute(REST_CONTEXT);
                if (restContext == null) {
                    restContext = RestContext.build();
                    context.setAttribute(REST_CONTEXT, restContext);
                }
            }
        }
        log.info("DispatcherServlet initialized");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RequestMappingModel mapping = restContext.matchRequest(req);
        if (mapping == null || mapping.getMethod() == null) {
            resp.sendError(404);
            return;
        }
        Method method = mapping.getMethod();
        String produces = mapping.getProduces();
        if (produces.isEmpty()) {
            produces = "text/html";
        }
        try {
            Object[] params = MethodParamsResolver.resolveAll(method, req, resp);
            String result = String.valueOf(method.invoke(getInstance(method.getDeclaringClass()), params));
            resp.setContentType(produces);
            resp.getOutputStream().print(result);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("Error invoking method " + method + ": " + e);
        } catch (MethodParamResolveException e) {
            throw new ServletException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getInstance(Class<? extends T> clazz) {
        Object instance = instances.get(clazz);
        if (instance != null && clazz.isInstance(instance)) {
            return (T) instance;
        }
        try {
            instance = clazz.newInstance();
            instances.put(clazz, instance);
            return (T) instance;
        } catch (InstantiationException | IllegalAccessException e) {
            log.error("Error instantiating " + clazz.getName() + ": " + e);
            return null;
        }
    }
}
