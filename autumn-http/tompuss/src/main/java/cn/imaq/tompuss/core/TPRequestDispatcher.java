package cn.imaq.tompuss.core;

import cn.imaq.tompuss.filter.TPFilterChain;
import cn.imaq.tompuss.servlet.TPHttpServletRequest;
import cn.imaq.tompuss.servlet.TPServletContext;
import cn.imaq.tompuss.servlet.TPServletRegistration;
import cn.imaq.tompuss.util.TPMatchResult;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import java.io.IOException;

@Slf4j
public class TPRequestDispatcher implements RequestDispatcher {
    private TPServletContext context;
    private String resPath;

    public TPRequestDispatcher(TPServletContext context, String resPath) {
        this.context = context;
        this.resPath = resPath;
    }

    @Override
    public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        if (request instanceof TPHttpServletRequest) {
            TPHttpServletRequest httpServletRequest = (TPHttpServletRequest) request;
            httpServletRequest.setDispatcherType(DispatcherType.FORWARD);
            httpServletRequest.setAttribute(FORWARD_REQUEST_URI, httpServletRequest.getRequestURI());
            httpServletRequest.setAttribute(FORWARD_CONTEXT_PATH, httpServletRequest.getContextPath());
            httpServletRequest.setAttribute(FORWARD_PATH_INFO, httpServletRequest.getPathInfo());
            httpServletRequest.setAttribute(FORWARD_SERVLET_PATH, httpServletRequest.getServletPath());
            httpServletRequest.setAttribute(FORWARD_QUERY_STRING, httpServletRequest.getQueryString());
        }
        response.resetBuffer();
        this.dispatch(request, response);
    }

    @Override
    public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        if (request instanceof TPHttpServletRequest) {
            ((TPHttpServletRequest) request).setDispatcherType(DispatcherType.INCLUDE);
        }
        this.dispatch(request, response);
    }

    public Result request(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        return this.dispatch(request, response);
    }

    public Result dispatch(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        // Match Servlet
        TPMatchResult<TPServletRegistration> servletMatch = context.matchServletByPath(resPath);
        ((TPHttpServletRequest) request).setMatchResult(servletMatch);
        if (servletMatch == null) {
            return Result.NOTFOUND;
        }
        Servlet servlet = servletMatch.getObject().getServletInstance();
        if (!(servlet instanceof HttpServlet)) {
            return Result.NOTFOUND;
        }
        context.getListeners(ServletRequestListener.class).forEach(x -> x.requestInitialized(new ServletRequestEvent(context, request)));
        // Match Filters
        TPFilterChain filterChain = context.matchFilters(resPath, servlet, request.getDispatcherType());
        try {
            filterChain.doFilter(request, response);
        } finally {
            context.getListeners(ServletRequestListener.class).forEach(x -> x.requestDestroyed(new ServletRequestEvent(context, request)));
        }
        return Result.SUCCESS;
    }

    enum Result {
        SUCCESS,
        NOTFOUND
    }
}
