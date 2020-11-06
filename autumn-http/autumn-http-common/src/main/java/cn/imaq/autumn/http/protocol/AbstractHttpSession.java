package cn.imaq.autumn.http.protocol;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class AbstractHttpSession {
    // Per-session variables
    private final int maxBodyLength;
    private final byte[] buf = new byte[2048];
    private int bufLimit;
    private State state = State.START;
    protected long lastActive = System.currentTimeMillis();

    // Per-request variables
    protected Map<String, List<String>> headersMap = new HashMap<>();
    private int contentLength = -1;
    protected boolean closeConnection = false;
    protected byte[] body;
    private int bodyLimit;

    protected AbstractHttpSession(int maxBodyLength) {
        this.maxBodyLength = maxBodyLength;
    }

    public void processByteBuffer(ByteBuffer byteBuf) throws IOException {
        if (bufLimit + byteBuf.limit() > buf.length) {
            bufLimit = 0;
        }
        byteBuf.get(buf, bufLimit, byteBuf.limit());
        bufLimit += byteBuf.limit();
        processBuf();
    }

    protected abstract boolean checkStart(String line);

    protected abstract void finish() throws IOException;

    protected abstract void error() throws IOException;

    protected abstract void close() throws IOException;

    private void finishAndReset() throws IOException {
        finish();

        this.state = State.START;
        this.bufLimit = 0;
        this.headersMap.clear();
        this.contentLength = -1;
        this.body = null;
        this.bodyLimit = 0;
    }

    protected void tryClose() {
        try {
            close();
        } catch (IOException e) {
            log.warn("Failed to close connection: {}", String.valueOf(e));
        }
    }

    protected void refreshLastActiveTime() {
        this.lastActive = System.currentTimeMillis();
    }

    private void processBuf() throws IOException {
        int readBytes = 0;
        if (state == State.START || state == State.HEADERS) {
            AutumnByteArrayReader reader = new AutumnByteArrayReader(buf, 0, bufLimit);
            String line;
            while ((line = reader.nextLine()) != null) {
                if (state == State.START) {
                    if (checkStart(line)) {
                        refreshLastActiveTime();
                        state = State.HEADERS;
                    } else if (!line.isEmpty()) {
                        error();
                        close();
                        return;
                    }
                } else if (state == State.HEADERS) {
                    if (line.isEmpty()) {
                        refreshLastActiveTime();

                        List<String> contentLengthHeaders = headersMap.get("content-length");
                        if (contentLengthHeaders != null) {
                            contentLength = Integer.parseInt(contentLengthHeaders.get(0));
                            if (contentLength > maxBodyLength) {
                                error();
                                close();
                                return;
                            }
                        }

                        List<String> connectionHeaders = headersMap.get("connection");
                        if (connectionHeaders != null) {
                            if (connectionHeaders.get(0).toLowerCase().equals("close")) {
                                closeConnection = true;
                            }
                        }

                        if (contentLength <= 0) {
                            finishAndReset();
                            return;
                        } else {
                            body = new byte[contentLength];
                            state = State.BODY;
                        }
                        break;
                    } else {
                        // expect: "Header-Key: Header-Value"
                        String[] kv = line.split(":", 2);
                        if (kv.length == 2) {
                            refreshLastActiveTime();
                            String key = kv[0].trim().toLowerCase();
                            String value = kv[1].trim();
                            headersMap.computeIfAbsent(key, k -> new LinkedList<>()).add(value);
                        } else {
                            error();
                            close();
                            return;
                        }
                    }
                }
            }
            readBytes += reader.getReadBytes();
        }

        if (state == State.BODY) {
            refreshLastActiveTime();
            int canRead = bufLimit - readBytes;
            if (canRead >= contentLength - bodyLimit) {
                canRead = contentLength - bodyLimit;
            }
            System.arraycopy(buf, readBytes, body, bodyLimit, canRead);
            readBytes += canRead;
            bodyLimit += canRead;
            if (bodyLimit >= contentLength) {
                finishAndReset();
                return;
            }
        }

        if (readBytes < bufLimit && readBytes > 0) {
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
