package cn.imaq.tompuss.core;

import cn.imaq.autumn.http.protocol.AutumnHttpRequest;
import cn.imaq.autumn.http.protocol.AutumnHttpResponse;
import cn.imaq.autumn.http.server.protocol.AutumnHttpHandler;
import cn.imaq.tompuss.http.TPHttpServletRequest;

import javax.servlet.http.HttpServletRequest;

public class TPDispatcher implements AutumnHttpHandler {
    private final byte[] INFO_404 = "<html><head><title>Not Found</title></head><body><center><h1>404 Not Found</h1></center><hr><center>TomPuss</center></body></html>".getBytes();

    private TPEngine engine;

    TPDispatcher(TPEngine engine) {
        this.engine = engine;
    }

    @Override
    public AutumnHttpResponse handle(AutumnHttpRequest request) {
        TPServletMapping mapping = engine.findServletByPath(request.getPath());
        if (mapping == null) {
            // 404 Not Found
            return AutumnHttpResponse.builder()
                    .status(404)
                    .contentType("text/html")
                    .body(INFO_404)
                    .build();
        }
        // TODO
        HttpServletRequest httpServletRequest = new TPHttpServletRequest(request, mapping, engine);
        return null;
    }
}
