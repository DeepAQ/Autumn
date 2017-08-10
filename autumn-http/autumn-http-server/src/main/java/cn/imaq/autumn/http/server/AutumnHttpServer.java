package cn.imaq.autumn.http.server;

import cn.imaq.autumn.http.server.util.AutumnHTTPBanner;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class AutumnHttpServer {
    private int port;

    private final int NUM_WORKERS = Runtime.getRuntime().availableProcessors();
    private final Worker[] workers = new Worker[NUM_WORKERS];
    private final AtomicInteger currentWorker = new AtomicInteger(0);
    private volatile boolean running = false;

    public AutumnHttpServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
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
            new Boss(sChannel).start();
            log.info("Started HTTP server on port " + port + " with " + NUM_WORKERS + " workers");
        }
    }

    public void stop() {
        running = false;
    }

    abstract class EventLoop extends Thread {
        private final Selector selector = Selector.open();
        private final Lock regLock = new ReentrantLock();

        EventLoop(String name) throws IOException {
            super(name);
        }

        void register(AbstractSelectableChannel channel, int op, Object attachment) {
            try {
                regLock.lock();
                selector.wakeup();
                channel.register(selector, op, attachment);
                regLock.unlock();
            } catch (ClosedChannelException e) {
                log.warn("Attempt to register a closed channel", e);
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

    class Boss extends EventLoop {
        Boss(ServerSocketChannel sChannel) throws IOException {
            super("Boss");
            register(sChannel, SelectionKey.OP_ACCEPT, null);
        }

        @Override
        void process(SelectionKey key) {
            if (key.isAcceptable()) {
                ServerSocketChannel sChannel = (ServerSocketChannel) key.channel();
                try {
                    SocketChannel cChannel = sChannel.accept();
                    cChannel.configureBlocking(false);
                    ByteBuffer buf = ByteBuffer.allocate(1024);
                    int workerIndex = currentWorker.getAndIncrement() % NUM_WORKERS;
                    workers[workerIndex].register(cChannel, SelectionKey.OP_READ, buf);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class Worker extends EventLoop {
        Worker(int index) throws IOException {
            super("Worker-" + index);
        }

        @Override
        void process(SelectionKey key) {
            if (key.isReadable()) {
                SocketChannel cChannel = (SocketChannel) key.channel();
                ByteBuffer buf = (ByteBuffer) key.attachment();
                try {
                    int readBytes = cChannel.read(buf);
                    if (readBytes < 0) {
                        cChannel.close();
                    } else if (readBytes > 0) {
                        buf.flip();
                        byte[] tmpBuf = new byte[buf.limit()];
                        buf.get(tmpBuf);
                        //log.info(new String(tmpBuf));
                        buf.compact();
                        cChannel.write(ByteBuffer.wrap("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: 12\r\n\r\nHello World!".getBytes()));
                    }
                } catch (IOException e) {
                    try {
                        cChannel.close();
                    } catch (IOException e1) {
                        log.warn("Failed to close channel", e1);
                    }
                }
            }
        }
    }
}
