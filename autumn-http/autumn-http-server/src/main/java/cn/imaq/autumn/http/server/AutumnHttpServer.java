package cn.imaq.autumn.http.server;

import cn.imaq.autumn.http.server.util.AutumnHTTPBanner;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class AutumnHttpServer {
    private int port;
    private Selector selector;
    private volatile boolean running = false;

    public AutumnHttpServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        if (!running) {
            AutumnHTTPBanner.printBanner();
            // Open channel
            selector = Selector.open();
            ServerSocketChannel sChannel = ServerSocketChannel.open();
            sChannel.configureBlocking(false);
            sChannel.bind(new InetSocketAddress(port));
            sChannel.register(selector, SelectionKey.OP_ACCEPT);
            running = true;
            new Worker().start();
        }
    }

    public void stop() {
        running = false;
    }

    class Worker extends Thread {
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
                        if (key.isAcceptable()) {
                            ServerSocketChannel sChannel = (ServerSocketChannel) key.channel();
                            try {
                                SocketChannel cChannel = sChannel.accept();
                                cChannel.configureBlocking(false);
                                ByteBuffer buf = ByteBuffer.allocate(1024);
                                cChannel.register(selector, SelectionKey.OP_READ, buf);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (key.isReadable()) {
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
                                    System.out.print(new String(tmpBuf));
                                    buf.compact();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }
}
