package cn.imaq.autumn.http.server.protocol;

import cn.imaq.autumn.http.protocol.AbstractHttpSession;
import cn.imaq.autumn.http.protocol.AutumnHttpRequest;
import cn.imaq.autumn.http.protocol.AutumnHttpResponse;
import cn.imaq.autumn.http.server.HttpServerOptions;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class AIOHttpServerSession extends AbstractHttpSession {
    private static final Set<String> VALID_METHODS = new HashSet<>();

    static {
        Collections.addAll(VALID_METHODS, "GET", "HEAD", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "TRACE");
    }

    private final AsynchronousSocketChannel cChannel;
    private final HttpServerOptions options;
    private final ByteBuffer buf;
    private String method, path, protocol;
    private final AtomicBoolean readPending = new AtomicBoolean(false);
    private final AtomicBoolean writePending = new AtomicBoolean(false);

    public AIOHttpServerSession(AsynchronousSocketChannel cChannel, HttpServerOptions options) {
        super(options.getMaxBodyBytes());
        this.cChannel = cChannel;
        this.options = options;
        this.buf = ByteBuffer.allocate(1024);
    }

    public void tryRead() {
        if (!readPending.compareAndSet(false, true)) {
            return;
        }

        buf.clear();
        cChannel.read(buf, options.getIdleTimeoutSeconds() * 1000L - (System.currentTimeMillis() - this.lastActive), TimeUnit.MILLISECONDS, null, new CompletionHandler<Integer, Object>() {
            @Override
            public void completed(Integer result, Object attachment) {
                readPending.set(false);
                writePending.set(false);
                boolean success = false;
                if (result > 0) {
                    buf.flip();
                    try {
                        processByteBuffer(buf);
                        success = true;
                    } catch (IOException e) {
                        log.warn("Failed to process buffer: {}", String.valueOf(e));
                    }
                }

                if (!success) {
                    tryClose();
                } else if (!writePending.get()) {
                    tryRead();
                }
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                readPending.set(false);
                log.warn("Failed to read: {}", String.valueOf(exc));
                tryClose();
            }
        });
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
        writePending.set(true);
        options.getExecutor().submit(() -> writeResponse(options.getHandler().handle(request)));
    }

    @Override
    protected void error() {
        writePending.set(true);
        writeResponse(AutumnHttpResponse.builder().status(400).build());
    }

    @Override
    protected void close() throws IOException {
        cChannel.close();
    }

    private void writeResponse(AutumnHttpResponse response) {
        cChannel.write(ByteBuffer.wrap(response.toHeaderBytes()), null, new CompletionHandler<Integer, Object>() {
            @Override
            public void completed(Integer result, Object attachment) {
                if (response.getBody() != null) {
                    cChannel.write(ByteBuffer.wrap(response.getBody()), null, new CompletionHandler<Integer, Object>() {
                        @Override
                        public void completed(Integer result, Object attachment) {
                            finishWriteResponse();
                        }

                        @Override
                        public void failed(Throwable exc, Object attachment) {
                            log.warn("Failed to write body: {}", String.valueOf(exc));
                            tryClose();
                        }
                    });
                } else {
                    finishWriteResponse();
                }
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                log.warn("Failed to write headers: {}", String.valueOf(exc));
                tryClose();
            }
        });
    }

    private void finishWriteResponse() {
        if (closeConnection) {
            tryClose();
        } else {
            refreshLastActiveTime();
            tryRead();
        }
    }
}
