package cn.imaq.tompuss;

import cn.imaq.tompuss.core.TPEngine;
import cn.imaq.tompuss.util.TPBanner;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

@Slf4j
public class TomPuss {
    @Getter
    @Setter
    private int port = 8080;

    @Getter
    private TPEngine engine;

    public void start() {
        synchronized (this) {
            if (this.engine != null) {
                this.engine.stop();
            }
            TPBanner.printBanner();
            this.engine = new TPEngine(port);
            log.info("Scanning Servlets in classpath ...");
            new FastClasspathScanner().matchClassesWithAnnotation(WebServlet.class, cls -> {
                if (HttpServlet.class.isAssignableFrom(cls)) {
                    WebServlet anno = cls.getAnnotation(WebServlet.class);
                    for (String path : anno.value()) {
                        this.engine.addServlet(path, (Class<? extends HttpServlet>) cls);
                    }
                    for (String path : anno.urlPatterns()) {
                        this.engine.addServlet(path, (Class<? extends HttpServlet>) cls);
                    }
//                    if (anno.loadOnStartup() >= 0) {
//                        this.engine.checkInitServlet((Class<? extends HttpServlet>) cls);
//                    }
                }
            }).scan();
            this.engine.start();
        }
    }

    public static void main(String[] args) {
        new TomPuss().start();
    }
}
