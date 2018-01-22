package cn.imaq.tompuss.filter;

import javax.servlet.*;
import java.io.IOException;
import java.util.Iterator;

public class TPFilterChain implements FilterChain {
    private Iterator<TPFilterRegistration> iterator;

    private Servlet servlet;

    public TPFilterChain(Iterable<TPFilterRegistration> filters, Servlet servlet) {
        this.iterator = filters.iterator();
        this.servlet = servlet;
    }

    /**
     * Causes the next filter in the chain to be invoked, or if the calling filter is the last filter
     * in the chain, causes the resource at the end of the chain to be invoked.
     *
     * @param request  the request to pass along the chain.
     * @param response the response to pass along the chain.
     * @throws IOException      if an I/O related error has occurred during the processing
     * @throws ServletException if an exception has occurred that interferes with the
     *                          filterChain's normal operation
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        if (iterator.hasNext()) {
            iterator.next().getFilterInstance().doFilter(request, response, this);
        } else if (servlet != null) {
            servlet.service(request, response);
        }
    }
}
