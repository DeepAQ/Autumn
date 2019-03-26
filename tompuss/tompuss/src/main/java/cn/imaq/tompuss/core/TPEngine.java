package cn.imaq.tompuss.core;

import cn.imaq.autumn.http.server.AutumnHttpServer;
import cn.imaq.tompuss.servlet.TPServletContext;
import cn.imaq.tompuss.util.TPMatchResult;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@NoArgsConstructor
public class TPEngine {
    @Getter
    @Setter
    private int port;

    private AutumnHttpServer httpServer;
    private Queue<TPServletContext> contexts = new ConcurrentLinkedQueue<>();

    public TPEngine(int port) {
        this.port = port;
    }

    public synchronized void start() {
        if (this.httpServer != null) {
            this.httpServer.stop();
        }

        try {
            this.httpServer = new AutumnHttpServer(port, new TPDispatcher(this));
            for (TPServletContext context : contexts) {
                context.startup();
            }
            this.httpServer.start();
            log.info("TomPuss Engine started on port {}", this.port);
        } catch (IOException e) {
            log.error("TomPuss Engine failed to start!", e);
        }
    }

    public synchronized void stop() {
        log.info("Stopping TomPuss Engine ...");
        if (this.httpServer != null) {
            this.httpServer.stop();
        }
    }

    public TPServletContext newWebApp(String appName, String contextPath, File resourceRoot) {
        TPServletContext context = new TPServletContext(this, appName, contextPath, resourceRoot);
        this.contexts.add(context);
        return context;
    }

    public TPMatchResult<TPServletContext> matchContextByPath(String path) {
        TPServletContext result = null;
        int bestLength = 0;
        for (TPServletContext context : contexts) {
            String contextPath = context.getContextPath();
            if (path.startsWith(contextPath) && contextPath.length() > bestLength) {
                bestLength = contextPath.length();
                result = context;
            }
        }
        if (result != null) {
            return new TPMatchResult<>(result.getContextPath(), result);
        } else {
            return null;
        }
    }
}
