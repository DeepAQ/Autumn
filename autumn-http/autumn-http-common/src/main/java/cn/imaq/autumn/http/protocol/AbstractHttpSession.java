package cn.imaq.autumn.http.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractHttpSession {
    private static final int MAX_BODY_LENGTH = 1024 * 1024 * 10;

    private State state = State.START;
    private long lastActive = System.currentTimeMillis();
    protected Map<String, List<String>> headersMap = new HashMap<>();
    private int contentLength = -1;

    private byte[] buf = new byte[2048];
    private int bufLimit = 0;
    protected byte[] body;
    private int bodyLimit = 0;

    public void processByteBuffer(ByteBuffer byteBuf) throws IOException {
        if (bufLimit + byteBuf.limit() > buf.length) {
            bufLimit = 0;
        }
        byteBuf.get(buf, bufLimit, byteBuf.limit());
        bufLimit += byteBuf.limit();
        processBuf();
    }

    public void checkIdle(int timeoutSec) throws IOException {
        if (System.currentTimeMillis() - lastActive > timeoutSec * 1000) {
            timeout();
        }
    }

    protected abstract boolean checkStart(String line);

    protected abstract void finish() throws IOException;

    protected abstract void error() throws IOException;

    protected abstract void timeout() throws IOException;

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
                    if (checkStart(line)) {
                        lastActive = System.currentTimeMillis();
                        state = State.HEADERS;
                        headersMap.clear();
                    }
                } else if (state == State.HEADERS) {
                    if (line.isEmpty()) {
                        if (contentLength < 0) {
                            lastActive = System.currentTimeMillis();
                            finish();
                            state = State.START;
                        } else {
                            state = State.BODY;
                        }
                        break;
                    } else {
                        // expect: "Header-Key: Header-Value"
                        String[] kv = line.split(":", 2);
                        if (kv.length == 2) {
                            lastActive = System.currentTimeMillis();
                            String key = kv[0].trim().toLowerCase();
                            String value = kv[1].trim();
                            headersMap.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
                            if (key.equals("content-length")) {
                                contentLength = Integer.valueOf(value);
                                if (contentLength > MAX_BODY_LENGTH) {
                                    error();
                                    state = State.START;
                                } else {
                                    body = new byte[contentLength];
                                    bodyLimit = 0;
                                }
                            }
                        } else {
                            error();
                            state = State.START;
                        }
                    }
                }
            }
        }
        if (state == State.BODY) {
            lastActive = System.currentTimeMillis();
            int canRead = bufLimit - readBytes;
            if (canRead >= contentLength - bodyLimit) {
                canRead = contentLength - bodyLimit;
            }
            System.arraycopy(buf, readBytes, body, bodyLimit, canRead);
            readBytes += canRead;
            bodyLimit += canRead;
            if (bodyLimit >= contentLength) {
                readBytes = bufLimit;
                finish();
                state = State.START;
            }
        }
        if (readBytes < bufLimit) {
            System.arraycopy(buf, readBytes, buf, 0, bufLimit - readBytes);
        }
        bufLimit -= readBytes;
    }

    protected enum State {
        START,
        HEADERS,
        BODY,
    }
}
