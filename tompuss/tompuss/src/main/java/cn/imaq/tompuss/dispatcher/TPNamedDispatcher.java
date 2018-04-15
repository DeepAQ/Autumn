package cn.imaq.tompuss.dispatcher;

import cn.imaq.tompuss.filter.TPFilterChain;
import cn.imaq.tompuss.servlet.TPServletContext;
import cn.imaq.tompuss.servlet.TPServletRegistration;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class TPNamedDispatcher extends TPRequestDispatcher {
    private TPServletRegistration servletRegistration;

    public TPNamedDispatcher(TPServletContext context, TPServletRegistration servletRegistration) {
        super(context);
        this.servletRegistration = servletRegistration;
    }

    @Override
    protected void dispatch(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        Servlet servlet = servletRegistration.getServletInstance();
        if (servlet == null || !(servlet instanceof HttpServlet)) {
            servlet = context.getDefaultServletRegistration().getServletInstance();
        }
        if (servlet == null || !(servlet instanceof HttpServlet)) {
            ((HttpServletResponse) response).sendError(404);
            return;
        }
        // Match Filters
        TPFilterChain filterChain = context.matchFilters(null, servlet, request.getDispatcherType());
        filterChain.doFilter(request, response);
    }
}
