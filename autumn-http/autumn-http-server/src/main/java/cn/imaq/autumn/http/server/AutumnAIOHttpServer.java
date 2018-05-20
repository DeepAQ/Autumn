package cn.imaq.autumn.http.server;

import cn.imaq.autumn.http.server.protocol.AIOHttpServerSession;
import cn.imaq.autumn.http.server.protocol.AutumnHttpHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

@Slf4j
public class AutumnAIOHttpServer {
    private final int NUM_WORKERS = Runtime.getRuntime().availableProcessors();
    private final int IDLE_TIMEOUT = 60;

    private int port;
    private AutumnHttpHandler handler;

    private final Object running = new Object();

    public AutumnAIOHttpServer(int port, AutumnHttpHandler handler) {
        this.port = port;
        this.handler = handler;
    }

    public void start() throws IOException {
        synchronized (running) {
            AsynchronousChannelGroup channelGroup = AsynchronousChannelGroup.withFixedThreadPool(NUM_WORKERS, Thread::new);
            // Open channel
            AsynchronousServerSocketChannel sChannel = AsynchronousServerSocketChannel.open(channelGroup);
            sChannel.bind(new InetSocketAddress(port));
            sChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
                @Override
                public void completed(AsynchronousSocketChannel result, Object attachment) {
                    AIOHttpServerSession session = new AIOHttpServerSession(handler, result);
                    session.tryRead();
                    sChannel.accept(null, this);
                }

                @Override
                public void failed(Throwable exc, Object attachment) {
                    log.warn("Failed to accept new connection: {}", String.valueOf(exc));
                    sChannel.accept(null, this);
                }
            });
            log.info("Started HTTP server on port {} with {} threads", port, NUM_WORKERS);
            try {
                running.wait();
            } catch (InterruptedException ignored) {
            }
            sChannel.close();
        }
    }

    public void stop() {
        synchronized (running) {
            log.info("HTTP server stopped");
            running.notify();
        }
    }
}
