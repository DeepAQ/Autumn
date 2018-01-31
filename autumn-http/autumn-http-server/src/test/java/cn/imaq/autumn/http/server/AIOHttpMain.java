package cn.imaq.autumn.http.server;

import cn.imaq.autumn.http.server.handler.TeapotEchoHandler;

import java.io.IOException;

public class AIOHttpMain {
    public static void main(String[] args) throws IOException {
        AutumnAIOHttpServer server = new AutumnAIOHttpServer(8802, new TeapotEchoHandler());
        server.start();
        System.out.println("test");
//        server.stop();
    }
}
