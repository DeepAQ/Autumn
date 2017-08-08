package cn.imaq.autumn.http.server;

import java.io.IOException;

public class HttpMain {

    public static void main(String[] args) throws IOException {
        new AutumnHttpServer(8802).start();
    }

}
