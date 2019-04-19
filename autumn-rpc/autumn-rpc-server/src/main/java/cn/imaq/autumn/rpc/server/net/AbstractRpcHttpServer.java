package cn.imaq.autumn.rpc.server.net;

import cn.imaq.autumn.rpc.server.handler.RpcRequestHandler;

import java.io.IOException;

public abstract class AbstractRpcHttpServer {
    protected String host;
    protected int port;
    protected RpcRequestHandler handler;

    public AbstractRpcHttpServer(String host, int port, RpcRequestHandler handler) {
        this.host = host;
        this.port = port;
        this.handler = handler;
    }

    public abstract void start() throws IOException;

    public abstract void stop() throws IOException;
}
