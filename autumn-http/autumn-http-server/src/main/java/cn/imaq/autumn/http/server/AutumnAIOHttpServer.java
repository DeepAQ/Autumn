package cn.imaq.autumn.http.server;

import cn.imaq.autumn.http.server.protocol.AIOHttpServerSession;
import cn.imaq.autumn.http.server.protocol.AutumnHttpHandler;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

@Slf4j
public class AutumnAIOHttpServer {
    @Setter
    private HttpServerOptions options;

    private final Object running = new Object();

    @Deprecated
    public AutumnAIOHttpServer(int port, AutumnHttpHandler handler) {
        this(HttpServerOptions.builder().port(port).handler(handler).build());
    }

    public AutumnAIOHttpServer(HttpServerOptions options) {
        this.options = options;
    }

    public void start() throws IOException {
        synchronized (running) {
            AsynchronousChannelGroup channelGroup = AsynchronousChannelGroup.withFixedThreadPool(options.getWorkerCount(), Thread::new);
            // Open channel
            AsynchronousServerSocketChannel sChannel = AsynchronousServerSocketChannel.open(channelGroup);
            sChannel.bind(new InetSocketAddress(options.getHost(), options.getPort()));
            sChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
                @Override
                public void completed(AsynchronousSocketChannel result, Object attachment) {
                    AIOHttpServerSession session = new AIOHttpServerSession(result, options);
                    session.tryRead();
                    sChannel.accept(null, this);
                }

                @Override
                public void failed(Throwable exc, Object attachment) {
                    log.warn("Failed to accept new connection: {}", String.valueOf(exc));
                    sChannel.accept(null, this);
                }
            });
            log.info("Started HTTP server with options {}", options);
            try {
                running.wait();
            } catch (InterruptedException ignored) {
            }
            sChannel.close();
            log.info("HTTP server stopped");
        }
    }

    public void stop() {
        synchronized (running) {
            running.notify();
        }
    }
}
