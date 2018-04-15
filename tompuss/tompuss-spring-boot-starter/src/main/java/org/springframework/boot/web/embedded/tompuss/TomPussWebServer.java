package org.springframework.boot.web.embedded.tompuss;

import cn.imaq.tompuss.core.TPEngine;
import cn.imaq.tompuss.util.TPBanner;
import lombok.Getter;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.WebServerException;

public class TomPussWebServer implements WebServer {
    private final int port;

    @Getter
    private final TPEngine engine = new TPEngine();

    public TomPussWebServer(int port) {
        this.port = port;
    }

    @Override
    public void start() throws WebServerException {
        engine.setPort(this.port);
        TPBanner.printBanner();
        engine.start();
    }

    @Override
    public void stop() throws WebServerException {
        engine.stop();
    }

    @Override
    public int getPort() {
        return this.port;
    }
}
