package cn.imaq.tompuss.dispatcher;

import cn.imaq.tompuss.servlet.TPHttpServletRequest;
import cn.imaq.tompuss.servlet.TPServletContext;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import java.io.IOException;

@Slf4j
public abstract class TPRequestDispatcher implements RequestDispatcher {
    protected TPServletContext context;

    TPRequestDispatcher(TPServletContext context) {
        this.context = context;
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
            TPHttpServletRequest httpServletRequest = (TPHttpServletRequest) request;
            httpServletRequest.setDispatcherType(DispatcherType.INCLUDE);
            httpServletRequest.setAttribute(INCLUDE_REQUEST_URI, httpServletRequest.getRequestURI());
            httpServletRequest.setAttribute(INCLUDE_CONTEXT_PATH, httpServletRequest.getContextPath());
            httpServletRequest.setAttribute(INCLUDE_PATH_INFO, httpServletRequest.getPathInfo());
            httpServletRequest.setAttribute(INCLUDE_SERVLET_PATH, httpServletRequest.getServletPath());
            httpServletRequest.setAttribute(INCLUDE_QUERY_STRING, httpServletRequest.getQueryString());
        }
        this.dispatch(request, response);
    }

    public void request(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        this.dispatch(request, response);
    }

    protected abstract void dispatch(ServletRequest request, ServletResponse response) throws ServletException, IOException;
}
