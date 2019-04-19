package cn.imaq.autumn.rpc.server.net;

import cn.imaq.autumn.http.protocol.AutumnHttpResponse;
import cn.imaq.autumn.rpc.server.handler.RpcRequestHandler;

import java.io.IOException;

public class AutumnHttpServer implements RpcHttpServer {
    private cn.imaq.autumn.http.server.AutumnHttpServer autumnHttpServer;

    @Override
    public void configure(String host, int port, RpcRequestHandler requestHandler) {
        autumnHttpServer = new cn.imaq.autumn.http.server.AutumnHttpServer(port, req -> {
            RpcHttpResponse response = requestHandler.handle(RpcHttpRequest.builder()
                    .method(req.getMethod())
                    .path(req.getPath())
                    .body(req.getBody())
                    .build()
            );
            return AutumnHttpResponse.builder()
                    .status(response.getCode())
                    .contentType(response.getContentType())
                    .body(response.getBody())
                    .build();
        });
    }

    @Override
    public void start() throws IOException {
        autumnHttpServer.start();
    }

    @Override
    public void stop() {
        autumnHttpServer.stop();
    }
}
