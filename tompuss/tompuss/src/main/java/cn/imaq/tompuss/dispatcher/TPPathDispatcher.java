package cn.imaq.tompuss.dispatcher;

import cn.imaq.tompuss.filter.TPFilterChain;
import cn.imaq.tompuss.servlet.TPHttpServletRequest;
import cn.imaq.tompuss.servlet.TPServletContext;
import cn.imaq.tompuss.servlet.TPServletRegistration;
import cn.imaq.tompuss.util.TPMatchResult;
import cn.imaq.tompuss.util.TPNotFoundException;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import java.io.IOException;

@Slf4j
public class TPPathDispatcher extends TPRequestDispatcher {
    private String path;

    public TPPathDispatcher(TPServletContext context, String path) {
        super(context);
        this.path = path;
    }

    @Override
    protected void dispatch(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        // Match Servlet
        TPMatchResult<TPServletRegistration> servletMatch = context.matchServletByPath(path);
        ((TPHttpServletRequest) request).setMatchResult(servletMatch);
        Servlet servlet = null;
        if (servletMatch != null) {
            servlet = servletMatch.getObject().getServletInstance();
        }
        if (!(servlet instanceof HttpServlet)) {
            servlet = context.getDefaultServletRegistration().getServletInstance();
        }
        if (!(servlet instanceof HttpServlet)) {
            throw new TPNotFoundException();
        }
        // Match Filters
        TPFilterChain filterChain = context.matchFilters(path, servlet, request.getDispatcherType());
        filterChain.doFilter(request, response);
    }
}
