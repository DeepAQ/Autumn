package cn.imaq.autumn.http.server;

import cn.imaq.autumn.http.server.protocol.AutumnHttpHandler;
import cn.imaq.autumn.http.server.protocol.HttpServerSession;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class AutumnHttpServer {
    @Setter
    private HttpServerOptions options;

    private Thread listener;
    private Thread cleaner;
    private final EventLoop[] workers;
    private final AtomicInteger currentWorker = new AtomicInteger(0);
    private final Collection<WeakReference<HttpServerSession>> sessions = new ConcurrentLinkedQueue<>();

    private volatile boolean running = false;

    @Deprecated
    public AutumnHttpServer(int port, AutumnHttpHandler handler) {
        this(HttpServerOptions.builder().port(port).handler(handler).build());
    }

    public AutumnHttpServer(HttpServerOptions options) {
        this.options = options;
        this.workers = new EventLoop[options.getWorkerCount()];
    }

    public void start() throws IOException {
        synchronized (this) {
            if (!running) {
                // Open channel
                ServerSocketChannel sChannel = ServerSocketChannel.open();
                sChannel.configureBlocking(false);
                sChannel.bind(new InetSocketAddress(options.getHost(), options.getPort()));
                // Start threads
                running = true;
                for (int i = 0; i < options.getWorkerCount(); i++) {
                    workers[i] = new Worker(i);
                    workers[i].start();
                }
                listener = new Listener(sChannel);
                listener.start();
                cleaner = new IdleCleaner();
                cleaner.start();
                log.info("Started HTTP server with options {}", options);
            }
        }
    }

    public void stop() {
        synchronized (this) {
            if (running) {
                running = false;
                for (int i = 0; i < options.getWorkerCount(); i++) {
                    workers[i].interrupt();
                }
                listener.interrupt();
                cleaner.interrupt();
                sessions.clear();
                log.info("HTTP server stopped");
            }
        }
    }

    abstract class EventLoop extends Thread {
        private final Selector selector = Selector.open();
        private final Lock regLock = new ReentrantLock();

        EventLoop(String name) throws IOException {
            super(name);
        }

        void register(AbstractSelectableChannel channel, int op, Object attachment) {
            regLock.lock();
            try {
                selector.wakeup();
                channel.register(selector, op, attachment);
            } catch (ClosedChannelException e) {
                log.warn("Attempt to register a closed channel", e);
            } finally {
                regLock.unlock();
            }
        }

        abstract void process(SelectionKey key);

        @Override
        public void run() {
            while (running) {
                int count = 0;
                try {
                    count = selector.select();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (count > 0) {
                    Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
                    while (keyIter.hasNext()) {
                        SelectionKey key = keyIter.next();
                        keyIter.remove();
                        if (!key.isValid()) {
                            continue;
                        }
                        process(key);
                    }
                }
                regLock.lock();
                regLock.unlock();
            }
        }
    }

    class Listener extends EventLoop {
        Listener(ServerSocketChannel sChannel) throws IOException {
            super("AutumnHTTP-" + options.getPort() + "-Listener");
            setPriority(MAX_PRIORITY);
            register(sChannel, SelectionKey.OP_ACCEPT, null);
        }

        @Override
        void process(SelectionKey key) {
            if (key.isAcceptable()) {
                ServerSocketChannel sChannel = (ServerSocketChannel) key.channel();
                try {
                    SocketChannel cChannel = sChannel.accept();
                    cChannel.configureBlocking(false);
                    int workerIndex = currentWorker.getAndIncrement() % options.getWorkerCount();
                    HttpServerSession session = new HttpServerSession(cChannel, options);
                    workers[workerIndex].register(cChannel, SelectionKey.OP_READ, session);
                    sessions.add(new WeakReference<>(session));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class Worker extends EventLoop {
        private ByteBuffer buf = ByteBuffer.allocateDirect(1024);

        Worker(int index) throws IOException {
            super("AutumnHTTP-" + options.getPort() + "-Worker-" + index);
        }

        @Override
        void process(SelectionKey key) {
            if (key.isReadable()) {
                SocketChannel cChannel = (SocketChannel) key.channel();
                HttpServerSession session = (HttpServerSession) key.attachment();
                try {
                    buf.clear();
                    int readBytes = cChannel.read(buf);
                    if (readBytes < 0) {
                        cChannel.close();
                    } else if (readBytes > 0) {
                        buf.flip();
                        session.processByteBuffer(buf);
                    }
                } catch (Exception e) {
                    log.error("Got exception while processing request: {}", e.getClass().getName());
                    try {
                        cChannel.close();
                    } catch (IOException e1) {
                        log.warn("Failed to close channel", e1);
                    }
                }
            }
        }
    }

    class IdleCleaner extends Thread {
        public IdleCleaner() {
            super("AutumnHTTP-" + options.getPort() + "-IdleCleaner");
        }

        @Override
        public void run() {
            while (!this.isInterrupted()) {
                Iterator<WeakReference<HttpServerSession>> it = sessions.iterator();
                while (it.hasNext()) {
                    HttpServerSession session = it.next().get();
                    if (session == null || session.checkIdle(options.getIdleTimeoutSeconds())) {
                        it.remove();
                    }
                }
                try {
                    Thread.sleep(options.getIdleTimeoutSeconds() * 500);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
}
