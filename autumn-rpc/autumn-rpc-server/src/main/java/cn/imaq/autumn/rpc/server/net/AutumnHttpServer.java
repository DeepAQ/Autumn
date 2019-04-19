package cn.imaq.autumn.rpc.server.net;

import cn.imaq.autumn.http.protocol.AutumnHttpResponse;
import cn.imaq.autumn.rpc.server.handler.RpcRequestHandler;

import java.io.IOException;

public class AutumnHttpServer extends AbstractRpcHttpServer {
    private cn.imaq.autumn.http.server.AutumnHttpServer autumnHttpServer;

    public AutumnHttpServer(String host, int port, RpcRequestHandler handler) {
        super(host, port, handler);
        autumnHttpServer = new cn.imaq.autumn.http.server.AutumnHttpServer(port, req -> {
            RpcHttpResponse response = handler.handle(RpcHttpRequest.builder()
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
