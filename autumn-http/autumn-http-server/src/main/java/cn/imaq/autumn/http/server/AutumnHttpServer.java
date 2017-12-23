package cn.imaq.autumn.http.server;

import cn.imaq.autumn.http.server.protocol.AutumnHttpHandler;
import cn.imaq.autumn.http.server.protocol.HttpServerSession;
import cn.imaq.autumn.http.server.util.AutumnHTTPBanner;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class AutumnHttpServer {
    private final int NUM_WORKERS = Runtime.getRuntime().availableProcessors();
    private final int IDLE_TIMEOUT = 60;

    private int port;
    private AutumnHttpHandler handler;

    private EventLoop boss;
    private final IdleCleaner cleaner = new IdleCleaner();
    private final EventLoop[] workers = new Worker[NUM_WORKERS];
    private final AtomicInteger currentWorker = new AtomicInteger(0);
    private final Set<WeakReference<HttpServerSession>> sessions = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private volatile boolean running = false;

    public AutumnHttpServer(int port, AutumnHttpHandler handler) {
        this.port = port;
        this.handler = handler;
    }

    public void start() throws IOException {
        synchronized (this) {
            if (!running) {
                AutumnHTTPBanner.printBanner();
                // Open channel
                ServerSocketChannel sChannel = ServerSocketChannel.open();
                sChannel.configureBlocking(false);
                sChannel.bind(new InetSocketAddress(port));
                // Start threads
                running = true;
                for (int i = 0; i < NUM_WORKERS; i++) {
                    workers[i] = new Worker(i);
                    workers[i].start();
                }
                boss = new Boss(sChannel);
                boss.start();
                cleaner.start();
                log.info("Started HTTP server on port " + port + " with " + NUM_WORKERS + " workers");
            }
        }
    }

    public void stop() {
        synchronized (this) {
            if (running) {
                running = false;
                for (int i = 0; i < NUM_WORKERS; i++) {
                    workers[i].interrupt();
                }
                boss.interrupt();
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
            }
            regLock.unlock();
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

    class Boss extends EventLoop {
        Boss(ServerSocketChannel sChannel) throws IOException {
            super("Boss");
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
                    int workerIndex = currentWorker.getAndIncrement() % NUM_WORKERS;
                    HttpServerSession session = new HttpServerSession(handler, cChannel);
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
            super("Worker-" + index);
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
                } catch (IOException e) {
                    log.error("Got exception while processing request: " + e.getClass().getName());
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
            super("IdleCleaner");
        }

        @Override
        public void run() {
            while (true) {
                Iterator<WeakReference<HttpServerSession>> it = sessions.iterator();
                while (it.hasNext()) {
                    HttpServerSession session = it.next().get();
                    if (session == null) {
                        it.remove();
                    } else {
                        try {
                            session.checkIdle(IDLE_TIMEOUT);
                        } catch (IOException e) {
                            log.error("Error checking idle", e);
                        }
                    }
                }
                try {
                    Thread.sleep(IDLE_TIMEOUT * 500);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
}
