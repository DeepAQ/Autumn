package cn.imaq.autumn.rest.servlet;

import cn.imaq.autumn.rest.core.RestContext;
import cn.imaq.autumn.rest.message.MessageConverter;
import cn.imaq.autumn.rest.model.ExceptionHandlerModel;
import cn.imaq.autumn.rest.model.RequestMappingModel;
import cn.imaq.autumn.rest.param.resolver.MethodParamsResolver;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

@Slf4j
public class DispatcherServlet extends HttpServlet {
    private RestContext restContext;
    private MethodParamsResolver paramsResolver = new MethodParamsResolver();

    @Override
    public void init() throws ServletException {
        ServletContext context = this.getServletContext();
        restContext = (RestContext) context.getAttribute(RestContext.ATTR);
        if (restContext == null) {
            synchronized (context) {
                restContext = (RestContext) context.getAttribute(RestContext.ATTR);
                if (restContext == null) {
                    restContext = RestContext.build();
                    context.setAttribute(RestContext.ATTR, restContext);
                }
            }
        }
        log.info("DispatcherServlet initialized");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RequestMappingModel rmModel = restContext.matchRequestMapping(req);
        if (rmModel == null || rmModel.getMethod() == null) {
            resp.sendError(404);
            return;
        }
        Method rmMethod = rmModel.getMethod();
        Class<?> controllerClass = rmMethod.getDeclaringClass();
        HandlerResult result;
        try {
            // invoke controller method
            Object[] rmParams = paramsResolver.resolveAll(rmMethod, req, resp);
            Object controller = restContext.getApplicationContext().getBeanByType(controllerClass);
            result = invokeHandlerMethod(controller, rmMethod, rmParams, rmModel.getProduces(), rmModel.getConverter());
        } catch (Throwable t) {
            log.error("Exception invoking {}: {}", rmMethod, String.valueOf(t));
            // look for exception handler
            ExceptionHandlerModel ehModel = restContext.matchExceptionHandler(controllerClass, t.getClass());
            if (ehModel == null) {
                throw new ServletException(t);
            }
            // invoke exception handler method
            Method ehMethod = ehModel.getMethod();
            try {
                Object[] ehParams = paramsResolver.resolveAllWithThrowable(ehMethod, req, resp, t);
                Object eHandler = restContext.getApplicationContext().getBeanByType(ehMethod.getDeclaringClass());
                result = invokeHandlerMethod(eHandler, ehMethod, ehParams, ehModel.getProduces(), ehModel.getConverter());
                resp.setStatus(ehModel.getStatusCode());
            } catch (Throwable t1) {
                log.error("Exception when handing exception {}: {}", String.valueOf(t), String.valueOf(t1));
                throw new ServletException(t1);
            }
        }
        resp.setContentType(result.getContentType());
        resp.getOutputStream().write(result.getBody());
    }

    private HandlerResult invokeHandlerMethod(Object handler, Method method, Object[] params, String produces, Class<? extends MessageConverter> converterClass) throws Throwable {
        byte[] body;
        String contentType = produces;
        Object result;
        try {
            result = method.invoke(handler, params);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
        if (result instanceof String) {
            body = ((String) result).getBytes(StandardCharsets.UTF_8);
        } else if (result instanceof byte[]) {
            body = ((byte[]) result);
        } else {
            MessageConverter converter = restContext.getInstance(converterClass);
            body = converter.toBytes(result);
            contentType = converter.getContentType();
        }
        return new HandlerResult(contentType, body);
    }

    @Data
    @AllArgsConstructor
    private class HandlerResult {
        private String contentType;

        private byte[] body;
    }
}
