package cn.imaq.autumn.http.server.handler;

import cn.imaq.autumn.http.server.protocol.AutumnHttpHandler;
import cn.imaq.autumn.http.server.protocol.AutumnHttpRequest;
import cn.imaq.autumn.http.server.protocol.AutumnHttpResponse;

public class TeapotEchoHandler implements AutumnHttpHandler {
    @Override
    public AutumnHttpResponse handle(AutumnHttpRequest request) {
        byte[] body = request.getBody();
        if (body == null) {
            body = "<h1>I'm a teapot</h1>".getBytes();
        }
        return AutumnHttpResponse.builder()
                .status(418)
                .contentType("text/html")
                .body(body)
                .build();
    }
}
