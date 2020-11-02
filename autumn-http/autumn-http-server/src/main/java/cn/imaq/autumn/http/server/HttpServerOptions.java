package cn.imaq.autumn.http.server;

import cn.imaq.autumn.http.server.protocol.AutumnHttpHandler;
import lombok.Builder;
import lombok.Data;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Data
@Builder
public class HttpServerOptions {
    private AutumnHttpHandler handler;

    @Builder.Default
    private String host = "0.0.0.0";

    @Builder.Default
    private int port = 8080;

    @Builder.Default
    private int workerCount = Runtime.getRuntime().availableProcessors();

    @Builder.Default
    private int idleTimeoutSeconds = 60;

    @Builder.Default
    private ExecutorService executor = new ThreadPoolExecutor(0, 100, 1L, TimeUnit.MINUTES, new SynchronousQueue<>());

    @Builder.Default
    private int maxBodyBytes = 1024 * 1024;
}
