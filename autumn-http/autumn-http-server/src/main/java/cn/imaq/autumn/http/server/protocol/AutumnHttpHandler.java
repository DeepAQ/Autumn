package cn.imaq.autumn.http.server.protocol;

import cn.imaq.autumn.http.protocol.AutumnHttpRequest;
import cn.imaq.autumn.http.protocol.AutumnHttpResponse;

public interface AutumnHttpHandler {
    AutumnHttpResponse handle(AutumnHttpRequest request);
}
