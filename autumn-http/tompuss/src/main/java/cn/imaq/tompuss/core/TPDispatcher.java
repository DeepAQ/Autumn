package cn.imaq.tompuss.core;

import cn.imaq.autumn.http.protocol.AutumnHttpRequest;
import cn.imaq.autumn.http.protocol.AutumnHttpResponse;
import cn.imaq.autumn.http.server.protocol.AutumnHttpHandler;

public class TPDispatcher implements AutumnHttpHandler {
    private final byte[] INFO_404 = "<html><head><title>Not Found</title></head><body><center><h1>404 Not Found</h1></center><hr><center>TomPuss</center></body></html>".getBytes();

    private TPEngine engine;

    TPDispatcher(TPEngine engine) {
        this.engine = engine;
    }

    @Override
    public AutumnHttpResponse handle(AutumnHttpRequest request) {
        // TODO
        return null;
    }
}
