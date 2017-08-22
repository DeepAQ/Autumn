package cn.imaq.autumn.http.client;

import cn.imaq.autumn.http.client.protocol.HttpConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class AutumnHttpClient {
    private static final ThreadLocal<Map<InetSocketAddress, HttpConnection>> localConnections = new ThreadLocal<>();
    private static final Pattern httpUrlPattern = Pattern.compile("^http://(.+?)[/(.*)]$", Pattern.CASE_INSENSITIVE);

    private static HttpConnection getConnection(InetSocketAddress address) {
        Map<InetSocketAddress, HttpConnection> connectionMap = localConnections.get();
        if (connectionMap == null) {
            connectionMap = new HashMap<>();
            localConnections.set(connectionMap);
        }
        HttpConnection conn = connectionMap.get(address);
        // reuse existing connections
        if (conn != null && conn.isAvailable()) {
            return conn;
        }
        // open new connection
        try {
            conn = new HttpConnection(address);
            connectionMap.put(address, conn);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return conn;
    }

    public static byte[] get(String urlStr, int timeoutMillis) throws IOException {
        URL url = new URL(urlStr);
        int port = url.getPort() > 0 ? url.getPort() : 80;
        InetSocketAddress socketAddress = new InetSocketAddress(url.getHost(), port);
        String path = url.getPath();
        if (path.isEmpty()) {
            path = "/";
        }
        String request = "GET " + path + " HTTP/1.1\r\nHost: " + url.getHost() + "\r\n\r\n";
        HttpConnection connection = getConnection(socketAddress);
        return connection.writeThenRead(request.getBytes(), timeoutMillis);
    }

    public static void main(String[] args) throws IOException {
        System.out.println(new String(AutumnHttpClient.get("http://www.baidu.com", 5000)));
    }
}
