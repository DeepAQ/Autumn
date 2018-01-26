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
    private int port = 8080;

    @Getter
    @Setter
    private File resourceRoot = new File(".");

    @Getter
    private TPEngine engine;

    public void start() {
        synchronized (this) {
            if (this.engine != null) {
                this.engine.stop();
            }
            TPBanner.printBanner();
            this.engine = new TPEngine(port);
            TPServletContext app = this.engine.newWebApp("root", "/", this.resourceRoot);
            app.loadConfigFile("WEB-INF/web.xml");
            app.scanAnnotations();
            app.enableJsp();
            this.engine.start();
        }
    }

    public static void main(String[] args) {
        new TomPuss().start();
    }
}
