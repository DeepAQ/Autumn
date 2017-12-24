package cn.imaq.tompuss.core;

import cn.imaq.autumn.http.server.AutumnHttpServer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class TPEngine {
    @Getter
    @Setter
    private int port;

    private AutumnHttpServer httpServer;
    private Map<String, Class<? extends HttpServlet>> pathMap = new ConcurrentHashMap<>();
    private Map<Class<? extends HttpServlet>, WeakReference<? extends HttpServlet>> servletMap = new ConcurrentHashMap<>();

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
        if (this.pathMap.containsKey(path)) {
            log.warn("Path \"" + path + "\" already has a Servlet mapping and will be replaced by " + servletClass.getName());
        }
        log.info("Adding path \"" + path + "\" -> " + servletClass.getName());
        this.pathMap.put(path, servletClass);
    }

    public void removeServlet(String path) {
        Class<? extends HttpServlet> servletClass = this.pathMap.remove(path);
        if (servletClass != null) {
            WeakReference<? extends HttpServlet> servletRef = this.servletMap.remove(servletClass);
            if (servletRef != null) {
                // thread-safe here
                HttpServlet servlet = servletRef.get();
                if (servlet != null) {
                    servlet.destroy();
                }
            }
        }
    }

    public HttpServlet checkInitServlet(Class<? extends HttpServlet> servletClass) {
        WeakReference<? extends HttpServlet> servletRef = this.servletMap.get(servletClass);
        synchronized (servletClass) {
            if (servletRef == null || servletRef.get() == null) {
                try {
                    log.info("Init Servlet " + servletClass.getName());
                    HttpServlet servlet = servletClass.newInstance();
                    servlet.init();
                    servletRef = new WeakReference<>(servlet);
                    this.servletMap.put(servletClass, servletRef);
                } catch (InstantiationException | IllegalAccessException e) {
                    log.error("Error instantiating Servlet " + servletClass.getName(), e);
                } catch (ServletException e) {
                    log.error("Error initiating Servlet " + servletClass.getName(), e);
                }
            }
        }
        return servletRef.get();
    }

    public HttpServlet findServletByPath(String path) {
        Class<? extends HttpServlet> servletClass = null;
        int bestLength = 0;
        for (String mapPath : pathMap.keySet()) {
            if (path.startsWith(mapPath) && mapPath.length() > bestLength) {
                bestLength = mapPath.length();
                servletClass = pathMap.get(mapPath);
            }
        }
        if (servletClass != null) {
            return this.checkInitServlet(servletClass);
        }
        return null;
    }
}
