package cn.imaq.autumn.http.client;

import cn.imaq.autumn.http.client.protocol.HttpConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AutumnHttpClient {
    private ThreadLocal<Map<InetSocketAddress, HttpConnection>> localConnections = new ThreadLocal<>();

    private HttpConnection getConnection(InetSocketAddress address) {
        if (localConnections.get() == null) {
            localConnections.set(new HashMap<>());
        }
        HttpConnection conn = localConnections.get().get(address);
        // reuse existing connections
        if (conn != null && conn.isAvailable()) {
            return conn;
        }
        // open new connection
        try {
            conn = new HttpConnection(address);
            localConnections.get().put(address, conn);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return conn;
    }

    public static void main(String[] args) {
        AutumnHttpClient client = new AutumnHttpClient();
        String request = "GET / HTTP/1.1\r\n\r\n";
        AtomicInteger count = new AtomicInteger();
        for (int i = 0; i < 8; i++) {
            new Thread(() -> {
                while (true) {
                    try {
                        client.getConnection(new InetSocketAddress("localhost", 8801))
                                .writeThenRead(request.getBytes(), 5000);
                        count.incrementAndGet();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        new Thread(() -> {
            while (true) {
                int lastCount = count.get();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(count.get() - lastCount);
            }
        }).start();
    }
}
