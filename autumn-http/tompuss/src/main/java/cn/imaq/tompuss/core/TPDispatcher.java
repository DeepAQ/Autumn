package cn.imaq.tompuss.core;

import cn.imaq.autumn.http.protocol.AutumnHttpRequest;
import cn.imaq.autumn.http.protocol.AutumnHttpResponse;
import cn.imaq.autumn.http.server.protocol.AutumnHttpHandler;
import cn.imaq.tompuss.servlet.TPHttpServletRequest;
import cn.imaq.tompuss.servlet.TPServletContext;
import cn.imaq.tompuss.servlet.TPServletRegistration;
import cn.imaq.tompuss.util.TPMatchResult;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

public class TPDispatcher implements AutumnHttpHandler {
    private static final byte[] INFO_404 = "<html><head><title>Not Found</title></head><body><center><h1>404 Not Found</h1></center><hr><center>TomPuss</center></body></html>".getBytes();

    private TPEngine engine;

    TPDispatcher(TPEngine engine) {
        this.engine = engine;
    }

    @Override
    public AutumnHttpResponse handle(AutumnHttpRequest request) {
        TPMatchResult<TPServletContext> contextMatch = engine.matchContextByPath(request.getPath());
        if (contextMatch == null) {
            return notFound();
        }
        TPServletContext context = contextMatch.getObject();
        String remainPath = request.getPath().substring(contextMatch.getMatched().length());
        // TODO filters
        TPMatchResult<TPServletRegistration> servletMatch = context.matchServletByPath(remainPath);
        if (servletMatch == null) {
            return notFound();
        }
        Servlet servlet = servletMatch.getObject().getServletInstance();
        if (!(servlet instanceof HttpServlet)) {
            return notFound();
        }
        HttpServletRequest req = new TPHttpServletRequest(request, engine, context);
        ((HttpServlet) servlet).service(req, resp);
        return null;
    }

    private static AutumnHttpResponse notFound() {
        return AutumnHttpResponse.builder()
                .protocol("HTTP/1.1")
                .status(404)
                .contentType("text/html")
                .body(INFO_404)
                .build();
    }
}
