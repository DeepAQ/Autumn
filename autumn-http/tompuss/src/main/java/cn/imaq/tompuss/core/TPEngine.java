package cn.imaq.tompuss.core;

import cn.imaq.autumn.http.server.AutumnHttpServer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class TPEngine {
    @Getter
    @Setter
    private int port;

    private AutumnHttpServer httpServer;
    private Map<String, TPServletMapping> pathMap = new ConcurrentHashMap<>();

    public TPEngine(int port) {
        this.port = port;
        this.httpServer = new AutumnHttpServer(port, new TPDispatcher(this));
    }

    public synchronized void start() {
        this.httpServer.stop();
        try {
            this.httpServer.start();
            log.info("TomPuss Engine started on port " + this.port);
        } catch (IOException e) {
            log.error("TomPuss Engine failed to start!", e);
        }
    }

    public synchronized void stop() {
        log.info("Stopping TomPuss Engine ...");
        this.httpServer.stop();
    }

    public void addServlet(String path, Class<? extends HttpServlet> servletClass) {
        while (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if (this.pathMap.containsKey(path)) {
            log.warn("Path \"" + path + "\" already has a Servlet mapping and will be replaced by " + servletClass.getName());
        }
        log.info("Adding path \"" + path + "\" -> " + servletClass.getName());
        this.pathMap.put(path, TPServletMapping.builder().path(path).servletClass(servletClass).build());
    }

    public void removeServlet(String path) {
        TPServletMapping servletMapping = this.pathMap.remove(path);
        if (servletMapping != null) {
            servletMapping.destroyServlet();
        }
    }

    public TPServletMapping findServletByPath(String path) {
        TPServletMapping mapping = null;
        int bestLength = 0;
        for (String mapPath : pathMap.keySet()) {
            if (path.startsWith(mapPath) && mapPath.length() > bestLength) {
                bestLength = mapPath.length();
                mapping = pathMap.get(mapPath);
            }
        }
        return mapping;
    }
}
