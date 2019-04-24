package cn.imaq.autumn.rpc.server;

import cn.imaq.autumn.core.context.AutumnContext;
import cn.imaq.autumn.rpc.server.config.RpcServerConfig;
import cn.imaq.autumn.rpc.server.handler.AutumnRpcRequestHandler;
import cn.imaq.autumn.rpc.server.net.RpcHttpServer;
import cn.imaq.autumn.rpc.util.AutumnRPCBanner;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class AutumnRPCServer {
    private AutumnContext context;
    private RpcServerConfig config;
    private RpcHttpServer httpServer;

    public AutumnRPCServer(AutumnContext context) {
        this.context = context;
    }

    public AutumnRPCServer(AutumnContext context, RpcServerConfig config) {
        this.context = context;
        this.config = config;
    }

    public synchronized void start() throws IOException {
        // Stop existing server
        stop();
        AutumnRPCBanner.printBanner();
        // Load config
        if (config == null) {
            config = context.getBeanByType(RpcServerConfig.class);
            if (config == null) {
                log.info("No RpcServerConfig beans found in Autumn context, using default config");
                config = RpcServerConfig.builder().build();
            }
        }
        // Start server
        AutumnRpcRequestHandler handler = new AutumnRpcRequestHandler(config, context);
        httpServer = config.getHttpServer();
        log.info("Using HTTP server: {}", httpServer.getClass().getSimpleName());
        httpServer.configure(config.getHost(), config.getPort(), handler);
        log.warn("Starting HTTP server ...");
        try {
            httpServer.start();
            log.warn("Bootstrap finished");
        } catch (IOException e) {
            log.error("Error starting server: {}", String.valueOf(e));
            throw e;
        }
    }

    public synchronized void stop() throws IOException {
        if (httpServer != null) {
            try {
                httpServer.stop();
            } catch (IOException e) {
                log.error("Error stopping server: {}", String.valueOf(e));
                throw e;
            }
        }
    }
}
