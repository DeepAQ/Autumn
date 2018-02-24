package cn.imaq.autumn.rest.servlet;

import cn.imaq.autumn.rest.core.RequestMappingModel;
import cn.imaq.autumn.rest.core.RestContext;
import cn.imaq.autumn.rest.message.MessageConverter;
import cn.imaq.autumn.rest.param.resolver.MethodParamsResolver;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Objects;

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
        RequestMappingModel mapping = restContext.matchRequest(req);
        if (mapping == null || mapping.getMethod() == null) {
            resp.sendError(404);
            return;
        }
        Method method = mapping.getMethod();
        String produces = mapping.getProduces();
        try {
            Object[] params = paramsResolver.resolveAll(method, req, resp);
            Object result = method.invoke(restContext.getApplicationContext().getBeanByType(method.getDeclaringClass()), params);
            byte[] resultBytes;
            if (result instanceof String) {
                resultBytes = ((String) result).getBytes();
            } else if (result instanceof byte[]) {
                resultBytes = ((byte[]) result);
            } else {
                Class<? extends MessageConverter> converterClass = mapping.getConverter();
                MessageConverter converter = restContext.getInstance(converterClass);
                resultBytes = Objects.requireNonNull(converter).toBytes(result);
                produces = converter.getContentType();
            }
            resp.setContentType(produces);
            resp.getOutputStream().write(resultBytes);
        } catch (Exception e) {
            log.error("Error invoking method {}: {}", method, String.valueOf(e));
            throw new ServletException(e);
        }
    }
}
