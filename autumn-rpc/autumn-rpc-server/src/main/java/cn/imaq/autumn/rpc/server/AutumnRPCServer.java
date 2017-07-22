package cn.imaq.autumn.rpc.server;

import cn.imaq.autumn.rpc.server.annotation.AutumnRPCExpose;
import cn.imaq.autumn.rpc.server.net.AutumnHttpServer;
import cn.imaq.autumn.rpc.server.util.AutumnRPCBanner;
import cn.imaq.autumn.rpc.server.util.ConfigUtil;
import cn.imaq.autumn.rpc.server.util.LogUtil;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.rapidoid.net.Server;

public class AutumnRPCServer {
    private static final AutumnHttpServer httpServer = new AutumnHttpServer();
    private static Server listeningServer;

    public static void start() {
        start(null);
    }

    public static void start(String configFile) {
        synchronized (httpServer) {
            // Stop existing server
            stop();
            // Load config
            AutumnRPCBanner.printBanner();
            ConfigUtil.loadConfig(configFile);
            // Scan classes with annotation
            LogUtil.W("Scanning services to expose ...");
            httpServer.getClassMap().clear();
            new FastClasspathScanner()
                    .matchClassesWithAnnotation(AutumnRPCExpose.class, clz -> {
                        LogUtil.I("Exposing: " + clz.getName());
                        httpServer.getClassMap().putClass(clz);
                        for (Class intf : clz.getInterfaces()) {
                            httpServer.getClassMap().putClass(intf.getName(), clz);
                        }
                    }).scan();
            // Start HTTP server
            String host = ConfigUtil.get("http.host");
            Integer port = Integer.valueOf(ConfigUtil.get("http.port"));
            LogUtil.W("Starting HTTP server on " + host + ":" + port);
            listeningServer = httpServer.listen(host, port);
            LogUtil.W("Bootstrap success");
        }
    }

    public static void stop() {
        synchronized (httpServer) {
            if (listeningServer != null && listeningServer.isActive()) {
                listeningServer.shutdown();
            }
        }
    }

    public static void main(String[] args) {
        start();
    }
}
