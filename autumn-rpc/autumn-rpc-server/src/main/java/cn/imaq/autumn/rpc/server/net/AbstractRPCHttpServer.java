package cn.imaq.autumn.rpc.server.net;

import cn.imaq.autumn.rpc.server.exception.AutumnHttpException;

public abstract class AbstractRPCHttpServer {
    protected String host;
    protected int port;
    protected RPCHttpHandler handler;

    public AbstractRPCHttpServer(String host, int port, RPCHttpHandler handler) {
        this.host = host;
        this.port = port;
        this.handler = handler;
    }

    public abstract void start() throws AutumnHttpException;

    public abstract void stop();
}
