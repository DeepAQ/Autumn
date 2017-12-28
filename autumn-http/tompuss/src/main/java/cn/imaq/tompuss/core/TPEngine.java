package cn.imaq.tompuss.core;

import cn.imaq.autumn.http.server.AutumnHttpServer;
import cn.imaq.tompuss.servlet.TPServletContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

@Slf4j
public class TPEngine {
    @Getter
    @Setter
    private int port;

    private AutumnHttpServer httpServer;
    private Queue<TPServletContext> contexts = new ConcurrentLinkedQueue<>();
    private Map<Class<? extends Servlet>, WeakReference<Servlet>> servletPool = new ConcurrentHashMap<>();

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

    private Servlet newServletInstance(Class<? extends Servlet> servletClass, ServletConfig config) {
        try {
            log.info("Init Servlet " + servletClass.getName());
            Servlet servlet = servletClass.newInstance();
            servlet.init(config);
            return servlet;
        } catch (InstantiationException | IllegalAccessException e) {
            log.error("Error instantiating Servlet " + servletClass.getName(), e);
        } catch (ServletException e) {
            log.error("Error initiating Servlet " + servletClass.getName(), e);
        }
        return null;
    }

    public Servlet checkAndGetServlet(Class<? extends Servlet> servletClass, Supplier<ServletConfig> configSupplier) {
        WeakReference<Servlet> servletRef = servletPool.get(servletClass);
        if (servletRef == null || servletRef.get() == null) {
            synchronized (servletClass) {
                if (servletRef == null || servletRef.get() == null) {
                    servletRef = new WeakReference<>(this.newServletInstance(servletClass, configSupplier.get()));
                    servletPool.put(servletClass, servletRef);
                }
            }
        }
        return servletRef.get();
    }

    public ServletContext newContext(String contextPath) {
        return new TPServletContext();
    }

    public ServletContext matchContextByPath(String path) {
        ServletContext result = null;
        int bestLength = 0;
        for (ServletContext context : contexts) {
            String contextPath = context.getContextPath();
            if (path.startsWith(contextPath) && contextPath.length() > bestLength) {
                bestLength = contextPath.length();
                result = context;
            }
        }
        return result;
    }
}
