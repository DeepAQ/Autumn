package cn.imaq.autumn.http.server;

import cn.imaq.autumn.http.server.handler.TeapotEchoHandler;

import java.io.IOException;

public class HttpMain {
    public static void main(String[] args) throws IOException {
        AutumnHttpServer server = new AutumnHttpServer(
                HttpServerOptions.builder()
                        .handler(new TeapotEchoHandler())
                        .host("0.0.0.0")
                        .port(8802)
                        .idleTimeoutSeconds(3600)
                        .build()
        );
        server.start();
//        server.stop();
    }
}
