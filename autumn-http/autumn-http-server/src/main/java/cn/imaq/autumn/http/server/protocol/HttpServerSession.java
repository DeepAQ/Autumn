package cn.imaq.autumn.http.server.protocol;

import cn.imaq.autumn.http.protocol.AbstractHttpSession;
import cn.imaq.autumn.http.protocol.AutumnHttpRequest;
import cn.imaq.autumn.http.protocol.AutumnHttpResponse;
import cn.imaq.autumn.http.server.HttpServerOptions;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class HttpServerSession extends AbstractHttpSession {
    private static final Set<String> VALID_METHODS = new HashSet<>();

    static {
        Collections.addAll(VALID_METHODS, "GET", "HEAD", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "TRACE");
    }

    private final SocketChannel cChannel;
    private final AutumnHttpHandler handler;
    private final ExecutorService executor;
    private String method, path, protocol;

    public HttpServerSession(SocketChannel cChannel, HttpServerOptions options) {
        super(options.getMaxBodyBytes());
        this.cChannel = cChannel;
        this.handler = options.getHandler();
        this.executor = options.getExecutor();
    }

    public boolean checkIdle(int timeoutSec) throws IOException {
        if (System.currentTimeMillis() - lastActive > timeoutSec * 1000) {
            close();
            return true;
        }
        return false;
    }

    @Override
    protected boolean checkStart(String line) {
        // expect: "GET /path/to/something HTTP/1.1"
        String[] words = line.split(" ", 3);
        if (words.length == 3 && VALID_METHODS.contains(words[0])) {
            method = words[0];
            path = words[1];
            protocol = words[2];
            return true;
        }
        return false;
    }

    @Override
    protected void finish() throws IOException {
        AutumnHttpRequest request = AutumnHttpRequest.builder()
                .method(method)
                .path(path)
                .protocol(protocol)
                .headers(headersMap)
                .body(body)
                .localAddress(cChannel.getLocalAddress())
                .remoteAddress(cChannel.getRemoteAddress())
                .build();
        try {
            executor.submit(() -> {
                try {
                    writeResponse(handler.handle(request));
                    if (closeConnection) {
                        close();
                    }
                } catch (IOException e) {
                    try {
                        close();
                    } catch (IOException ignored) {
                    }
                }
            });
        } catch (Exception e) {
            writeResponse(AutumnHttpResponse.builder().status(503).build());
            close();
        }
    }

    @Override
    protected void error() throws IOException {
        writeResponse(AutumnHttpResponse.builder().status(400).build());
    }

    @Override
    protected void close() throws IOException {
        cChannel.close();
    }

    private void writeResponse(AutumnHttpResponse response) throws IOException {
        cChannel.write(ByteBuffer.wrap(response.toHeaderBytes()));
        if (response.getBody() != null) {
            cChannel.write(ByteBuffer.wrap(response.getBody()));
        }
    }
}
