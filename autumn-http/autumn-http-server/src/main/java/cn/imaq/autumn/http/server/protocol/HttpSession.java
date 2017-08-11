package cn.imaq.autumn.http.server.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;

public class HttpSession {
    private static final Set<String> VALID_METHODS = new HashSet<>();
    private static final int MAX_BODY_LENGTH = 1024 * 1024 * 10;

    static {
        Collections.addAll(VALID_METHODS, "GET", "POST", "PUT", "DELETE");
    }

    private State state = State.START;
    private byte[] buf = new byte[2048];
    private int bufLimit = 0;

    private String method, path, protocol;
    private Map<String, List<String>> headersMap = new HashMap<>();
    private int contentLength = -1;
    private byte[] body;
    private int bodyLimit = 0;

    private AutumnHttpHandler handler;
    private SocketChannel cChannel;

    public HttpSession(AutumnHttpHandler handler, SocketChannel cChannel) {
        this.handler = handler;
        this.cChannel = cChannel;
    }

    public void processByteBuffer(ByteBuffer byteBuf) throws IOException {
        if (bufLimit + byteBuf.limit() > buf.length) {
            bufLimit = 0;
        }
        byteBuf.get(buf, bufLimit, byteBuf.limit());
        bufLimit += byteBuf.limit();
        processBuf();
    }

    private void processBuf() throws IOException {
        int readBytes = 0;
        if (state == State.START || state == State.HEADERS) {
            String[] lines = new String(buf, 0, bufLimit).split("\r\n", -1);
            if (lines.length <= 1) {
                return;
            }
            for (int i = 0; i < lines.length - 1; i++) {
                String line = lines[i];
                readBytes += line.length() + 2;
                if (state == State.START) {
                    // expect: "GET /path/to/something HTTP/1.1"
                    String[] words = line.split(" ", 3);
                    if (words.length == 3 && VALID_METHODS.contains(words[0])) {
                        method = words[0];
                        path = words[1];
                        protocol = words[2];
                        state = State.HEADERS;
                        headersMap.clear();
                    }
                } else if (state == State.HEADERS) {
                    if (line.isEmpty()) {
                        if (contentLength < 0) {
                            processRequest();
                            state = State.START;
                        } else {
                            state = State.BODY;
                        }
                        break;
                    } else {
                        // expect: "Header-Key: Header-Value"
                        String[] kv = line.split(":", 2);
                        if (kv.length == 2) {
                            String key = kv[0].trim().toLowerCase();
                            String value = kv[1].trim();
                            headersMap.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
                            if (key.equals("content-length")) {
                                contentLength = Integer.valueOf(value);
                                if (contentLength > MAX_BODY_LENGTH) {
                                    badRequest();
                                } else {
                                    body = new byte[contentLength];
                                    bodyLimit = 0;
                                }
                            }
                        } else {
                            badRequest();
                        }
                    }
                }
            }
        }
        if (state == State.BODY) {
            int canRead = bufLimit - readBytes;
            if (canRead >= contentLength - bodyLimit) {
                canRead = contentLength - bodyLimit;
            }
            System.arraycopy(buf, readBytes, body, bodyLimit, canRead);
            readBytes += canRead;
            bodyLimit += canRead;
            if (bodyLimit >= contentLength) {
                readBytes = bufLimit;
                processRequest();
                state = State.START;
            }
        }
        if (readBytes < bufLimit) {
            System.arraycopy(buf, readBytes, buf, 0, bufLimit - readBytes);
        }
        bufLimit -= readBytes;
    }

    private void processRequest() throws IOException {
        AutumnHttpRequest request = AutumnHttpRequest.builder()
                .method(method)
                .path(path)
                .protocol(protocol)
                .headers(headersMap)
                .body(body)
                .build();
        AutumnHttpResponse response = handler.handle(request);
        writeResponse(response);
    }

    private void writeResponse(AutumnHttpResponse response) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(response.getStatus()).append(' ')
                .append(AutumnHttpResponse.ResponseCodes.get(response.getStatus())).append("\r\n");
        if (response.getHeaders() != null) {
            for (Map.Entry<String, List<String>> header : response.getHeaders().entrySet()) {
                String key = header.getKey();
                for (String value : header.getValue()) {
                    sb.append(key).append(": ").append(value).append("\r\n");
                }
            }
        }
        if (response.getContentType() != null) {
            sb.append("Content-Type: ").append(response.getContentType()).append("\r\n");
        }
        if (response.getBody() != null) {
            sb.append("Content-Length: ").append(response.getBody().length).append("\r\n\r\n");
        }
        cChannel.write(ByteBuffer.wrap(sb.toString().getBytes()));
        if (response.getBody() != null) {
            cChannel.write(ByteBuffer.wrap(response.getBody()));
        }
    }

    private void badRequest() throws IOException {
        writeResponse(AutumnHttpResponse.builder()
                .status(400)
                .build()
        );
        state = State.START;
    }

    enum State {
        START,
        HEADERS,
        BODY,
    }
}
