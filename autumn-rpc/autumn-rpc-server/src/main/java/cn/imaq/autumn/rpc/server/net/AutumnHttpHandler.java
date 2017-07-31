package cn.imaq.autumn.rpc.server.net;

public interface AutumnHttpHandler {
    AutumnHttpResponse handle(AutumnHttpRequest request);
}
