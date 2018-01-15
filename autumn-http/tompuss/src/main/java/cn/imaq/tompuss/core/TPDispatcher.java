package cn.imaq.tompuss.core;

import cn.imaq.autumn.http.protocol.AutumnHttpRequest;
import cn.imaq.autumn.http.protocol.AutumnHttpResponse;
import cn.imaq.autumn.http.server.protocol.AutumnHttpHandler;
import cn.imaq.tompuss.filter.TPFilterChain;
import cn.imaq.tompuss.servlet.*;
import cn.imaq.tompuss.util.TPMatchResult;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

@Slf4j
public class TPDispatcher implements AutumnHttpHandler {
    private static final byte[] INFO_404 = "<html><head><title>Not Found</title></head><body><center><h1>404 Not Found</h1></center><hr><center>TomPuss</center></body></html>".getBytes();

    private TPEngine engine;

    TPDispatcher(TPEngine engine) {
        this.engine = engine;
    }

    @Override
    public AutumnHttpResponse handle(AutumnHttpRequest request) {
        String path = request.getPath().split("\\?", 2)[0];
        // Match context
        TPMatchResult<TPServletContext> contextMatch = engine.matchContextByPath(path);
        if (contextMatch == null) {
            return notFound();
        }
        TPServletContext context = contextMatch.getObject();
        // Match Servlet
        String remainPath = path.substring(contextMatch.getLength() - 1);
        TPMatchResult<TPServletRegistration> servletMatch = context.matchServletByPath(remainPath);
        if (servletMatch == null) {
            return notFound();
        }
        Servlet servlet = servletMatch.getObject().getServletInstance();
        if (!(servlet instanceof HttpServlet)) {
            return notFound();
        }
        // Build request and response
        TPHttpExchange exchange = new TPHttpExchange();
        TPHttpServletRequest req = new TPHttpServletRequest(request, context, exchange);
        TPHttpServletResponse resp = new TPHttpServletResponse(context, exchange);
        // Match Filters
        TPFilterChain filterChain = context.matchFilters(path, servletMatch.getObject().getName());
        try {
            filterChain.doFilter(req, resp);
            servlet.service(req, resp);
        } catch (Exception e) {
            log.warn("Exception in dispatcher", e);
            return error(e);
        }
        return resp.toAutumnHttpResponse();
    }

    private static AutumnHttpResponse notFound() {
        return AutumnHttpResponse.builder()
                .status(404)
                .contentType("text/html")
                .body(INFO_404)
                .build();
    }

    private static AutumnHttpResponse error(Exception e) {
        Writer writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer, true));
        return AutumnHttpResponse.builder()
                .status(500)
                .contentType("text/html")
                .body(("<html><head><title>Server Error</title></head><body><h1>Server Error</h1><pre>" +
                        writer.toString() +"</pre><hr>TomPuss</body></html>").getBytes())
                .build();
    }
}
