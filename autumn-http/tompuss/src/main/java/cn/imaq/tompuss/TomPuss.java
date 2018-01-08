package cn.imaq.tompuss;

import cn.imaq.tompuss.core.TPEngine;
import cn.imaq.tompuss.util.TPBanner;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
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
            ServletContext app = this.engine.newWebApp("root", "/", ".");
            log.info("Scanning Servlets in classpath ...");
            new FastClasspathScanner().matchClassesWithAnnotation(WebServlet.class, cls -> {
                if (HttpServlet.class.isAssignableFrom(cls)) {
                    WebServlet anno = cls.getAnnotation(WebServlet.class);
                    ServletRegistration registration = app.addServlet(anno.name().isEmpty() ? cls.getName() : anno.name(), (Class<? extends Servlet>) cls);
                    registration.addMapping(anno.value());
                    registration.addMapping(anno.urlPatterns());
                }
            }).scan();
            this.engine.start();
        }
    }

    public static void main(String[] args) {
        new TomPuss().start();
    }
}
