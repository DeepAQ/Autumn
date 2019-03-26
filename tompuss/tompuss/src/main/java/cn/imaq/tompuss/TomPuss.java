package cn.imaq.tompuss;

import cn.imaq.tompuss.core.TPEngine;
import cn.imaq.tompuss.servlet.TPServletContext;
import cn.imaq.tompuss.util.TPBanner;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class TomPuss {
    @Getter
    @Setter
    private boolean autoConfig = true;

    @Getter
    @Setter
    private boolean enableJsp = false;

    private TPEngine engine;
    private TPServletContext webApp;

    public TomPuss() {
        this(8080, new File("."));
    }

    public TomPuss(int port, File resourceRoot) {
        this.engine = new TPEngine(port);
        this.webApp = this.engine.newWebApp("root", "/", resourceRoot);
    }

    public TPEngine configureEngine() {
        return this.engine;
    }

    public TPServletContext configureWebApp() {
        return this.webApp;
    }

    public synchronized void start() {
        this.engine.stop();
        TPBanner.printBanner();
        if (autoConfig) {
            this.webApp.loadConfigFile("WEB-INF/web.xml");
            this.webApp.scanAnnotations();
        }
        if (enableJsp) {
            this.webApp.enableJsp();
        }
        this.engine.start();
    }

    public void stop() {
        this.engine.stop();
    }

    public static void main(String[] args) {
        new TomPuss().start();
    }
}
