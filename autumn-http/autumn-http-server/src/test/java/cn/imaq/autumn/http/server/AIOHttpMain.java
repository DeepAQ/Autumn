package cn.imaq.autumn.http.server;

import cn.imaq.autumn.http.server.handler.TeapotEchoHandler;

import java.io.IOException;

public class AIOHttpMain {
    public static void main(String[] args) throws IOException {
        AutumnAIOHttpServer server = new AutumnAIOHttpServer(
                HttpServerOptions.builder()
                        .handler(new TeapotEchoHandler())
                        .host("0.0.0.0")
                        .port(8802)
                        .idleTimeoutSeconds(60)
                        .build()
        );
        server.start();
        System.out.println("test");
//        server.stop();
    }
}
