package cn.imaq.autumn.rpc.server;

import cn.imaq.autumn.rpc.server.net.AutumnRPCHttpServer;
import cn.imaq.autumn.rpc.server.scanner.AutumnRPCScanner;
import cn.imaq.autumn.rpc.util.AutumnRPCBanner;
import cn.imaq.autumn.rpc.server.util.ConfigUtil;
import cn.imaq.autumn.rpc.util.LogUtil;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.rapidoid.net.Server;

public class AutumnRPCServer {
    private static final AutumnRPCHttpServer httpServer = new AutumnRPCHttpServer();
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
            // Scan services with scanners
            LogUtil.W("Scanning services to expose ...");
            httpServer.getInstanceMap().clear();
            new FastClasspathScanner().matchClassesImplementing(AutumnRPCScanner.class, scanner -> {
                LogUtil.W("Scanning with scanner " + scanner.getSimpleName());
                FastClasspathScanner classpathScanner = new FastClasspathScanner();
                try {
                    scanner.newInstance().process(classpathScanner, httpServer.getInstanceMap());
                    classpathScanner.scan();
                } catch (InstantiationException | IllegalAccessException e) {
                    LogUtil.E("Error instantiating scanner " + scanner.getSimpleName());
                }
            }).scan();
            // Start HTTP server
            String host = ConfigUtil.get("http.host");
            Integer port = Integer.valueOf(ConfigUtil.get("http.port"));
            LogUtil.W("Starting HTTP server on " + host + ":" + port);
            listeningServer = httpServer.start(host, port);
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
}
