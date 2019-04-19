package cn.imaq.autumn.rpc.server.net;

import cn.imaq.autumn.rpc.server.handler.RpcRequestHandler;

import java.io.IOException;

public interface RpcHttpServer {
    void configure(String host, int port, RpcRequestHandler requestHandler);

    void start() throws IOException;

    void stop() throws IOException;
}
