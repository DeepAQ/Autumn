package cn.imaq.autumn.http.server;

import cn.imaq.autumn.http.server.handler.TeapotEchoHandler;

import java.io.IOException;

public class HttpMain {
    public static void main(String[] args) throws IOException {
        new AutumnHttpServer(8802, new TeapotEchoHandler()).start();
    }
}
