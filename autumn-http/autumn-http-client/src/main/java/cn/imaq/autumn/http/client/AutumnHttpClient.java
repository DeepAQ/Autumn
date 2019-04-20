package cn.imaq.autumn.http.client;

import cn.imaq.autumn.http.client.protocol.HttpConnection;
import cn.imaq.autumn.http.protocol.AutumnHttpRequest;
import cn.imaq.autumn.http.protocol.AutumnHttpResponse;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class AutumnHttpClient {
    private static final ThreadLocal<Map<InetSocketAddress, HttpConnection>> localConnections = new ThreadLocal<>();
    private static final Pattern httpUrlPattern = Pattern.compile("^http://(.+?)[/(.*)]$", Pattern.CASE_INSENSITIVE);

    private static HttpConnection getConnection(InetSocketAddress address) throws IOException {
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
        conn = new HttpConnection(address);
        connectionMap.put(address, conn);
        return conn;
    }

    public static AutumnHttpResponse request(AutumnHttpRequest request, InetSocketAddress dest, int timeoutMillis) throws IOException {
        HttpConnection connection = getConnection(dest);
        return connection.writeThenRead(request.toRequestBytes(), timeoutMillis);
    }

    public static AutumnHttpResponse request(String method, String urlStr, String contentType, byte[] body, int timeoutMillis) throws IOException {
        URL url = new URL(urlStr);
        if (!"http".equals(url.getProtocol())) {
            throw new MalformedURLException("Only http protocol is supported");
        }

        String path = url.getPath();
        if (path.isEmpty()) {
            path = "/";
        }
        if (url.getQuery() != null) {
            path = path + "?" + url.getQuery();
        }

        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Host", Collections.singletonList(url.getHost()));
        if (contentType != null) {
            headers.put("Content-Type", Collections.singletonList(contentType));
        }
        AutumnHttpRequest request = AutumnHttpRequest.builder()
                .method(method)
                .path(path)
                .protocol("HTTP/1.1")
                .headers(headers)
                .body(body)
                .build();
        int port = url.getPort() > 0 ? url.getPort() : 80;
        InetSocketAddress socketAddress = new InetSocketAddress(url.getHost(), port);
        return request(request, socketAddress, timeoutMillis);
    }

    public static AutumnHttpResponse get(String urlStr, int timeoutMillis) throws IOException {
        return request("GET", urlStr, null, null, timeoutMillis);
    }

    public static AutumnHttpResponse post(String urlStr, String contentType, byte[] body, int timeoutMillis) throws IOException {
        return request("POST", urlStr, contentType, body, timeoutMillis);
    }
}
