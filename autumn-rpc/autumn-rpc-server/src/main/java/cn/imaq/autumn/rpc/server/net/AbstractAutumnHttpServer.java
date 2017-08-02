package cn.imaq.autumn.rpc.server.net;

import cn.imaq.autumn.rpc.server.exception.AutumnHttpException;

public abstract class AbstractAutumnHttpServer {
    protected String host;
    protected int port;
    protected AutumnHttpHandler handler;

    public AbstractAutumnHttpServer(String host, int port, AutumnHttpHandler handler) {
        this.host = host;
        this.port = port;
        this.handler = handler;
    }

    public abstract void start() throws AutumnHttpException;

    public abstract void stop();
}
