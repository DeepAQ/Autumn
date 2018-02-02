package cn.imaq.autumn.rest.servlet;

import cn.imaq.autumn.rest.core.RestContext;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class DispatcherServlet extends HttpServlet {
    private static final String REST_CONTEXT = RestContext.class.getName();

    private RestContext restContext;

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
    }
}
