package cn.imaq.autumn.rpc.server.net;

import cn.imaq.autumn.http.protocol.AutumnHttpResponse;
import cn.imaq.autumn.rpc.server.exception.AutumnHttpException;

import java.io.IOException;

public class AutumnHttpServer extends AbstractAutumnHttpServer {
    private cn.imaq.autumn.http.server.AutumnHttpServer autumnHttpServer;

    public AutumnHttpServer(String host, int port, AutumnHttpHandler handler) {
        super(host, port, handler);
        autumnHttpServer = new cn.imaq.autumn.http.server.AutumnHttpServer(port, req -> {
            RPCHttpResponse response = handler.handle(RPCHttpRequest.builder()
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
    public void start() throws AutumnHttpException {
        try {
            autumnHttpServer.start();
        } catch (IOException e) {
            throw new AutumnHttpException(e);
        }
    }

    @Override
    public void stop() {
        autumnHttpServer.stop();
    }
}
