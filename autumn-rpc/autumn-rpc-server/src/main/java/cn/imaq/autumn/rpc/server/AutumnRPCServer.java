package cn.imaq.autumn.rpc.server;

import cn.imaq.autumn.rpc.server.net.AutumnRPCHttpServer;
import cn.imaq.autumn.rpc.server.scanner.AutumnRPCScanner;
import cn.imaq.autumn.rpc.util.AutumnRPCBanner;
import cn.imaq.autumn.rpc.util.PropertiesUtils;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import lombok.extern.slf4j.Slf4j;
import org.rapidoid.net.Server;

import java.io.IOException;

@Slf4j
public class AutumnRPCServer {
    private static final String DEFAULT_CONFIG = "autumn-rpc-server-default.properties";

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
            httpServer.getConfig().clear();
            try {
                PropertiesUtils.load(httpServer.getConfig(), DEFAULT_CONFIG, configFile);
            } catch (IOException e) {
                log.error("Error loading config: " + e.toString());
            }
            // Scan services with scanners
            log.warn("Scanning services to expose ...");
            httpServer.getInstanceMap().clear();
            new FastClasspathScanner().matchClassesImplementing(AutumnRPCScanner.class, scanner -> {
                log.warn("Scanning with scanner " + scanner.getSimpleName());
                FastClasspathScanner classpathScanner = new FastClasspathScanner();
                try {
                    scanner.newInstance().process(classpathScanner, httpServer.getInstanceMap());
                    classpathScanner.scan();
                } catch (InstantiationException | IllegalAccessException e) {
                    log.error("Error instantiating scanner " + scanner.getSimpleName());
                }
            }).scan();
            // Start HTTP server
            log.warn("Starting HTTP server ...");
            listeningServer = httpServer.start();
            log.warn("Bootstrap success");
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
