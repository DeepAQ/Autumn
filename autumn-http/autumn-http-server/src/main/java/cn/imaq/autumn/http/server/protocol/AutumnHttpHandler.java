package cn.imaq.autumn.http.server.protocol;

public interface AutumnHttpHandler {
    AutumnHttpResponse handle(AutumnHttpRequest request);
}
