package cn.imaq.autumn.http.server.protocol;

import cn.imaq.autumn.http.protocol.AbstractHttpSession;
import cn.imaq.autumn.http.protocol.AutumnHttpRequest;
import cn.imaq.autumn.http.protocol.AutumnHttpResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class AIOHttpServerSession extends AbstractHttpSession {
    private static final Set<String> VALID_METHODS = new HashSet<>();

    static {
        Collections.addAll(VALID_METHODS, "GET", "POST", "PUT", "DELETE");
    }

    private AutumnHttpHandler handler;
    private AsynchronousSocketChannel cChannel;
    private ByteBuffer buf;
    private String method, path, protocol;

    public AIOHttpServerSession(AutumnHttpHandler handler, AsynchronousSocketChannel cChannel) {
        super(1024 * 1024);
        this.handler = handler;
        this.cChannel = cChannel;
        this.buf = ByteBuffer.allocate(1024);
    }

    public void tryRead() {
        cChannel.read(buf, null, new CompletionHandler<Integer, Object>() {
            @Override
            public void completed(Integer result, Object attachment) {
                if (result > 0) {
                    buf.flip();
                    try {
                        processByteBuffer(buf);
                    } catch (IOException e) {
                        log.warn("Failed to process buffer: {}", String.valueOf(e));
                        tryClose();
                    }
                    buf.clear();
                } else {
                    tryClose();
                }
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                log.warn("Failed to read: {}", String.valueOf(exc));
                tryClose();
            }
        });
    }

    public void tryClose() {
        try {
            cChannel.close();
        } catch (IOException e) {
            log.warn("Failed to close channel: {}", String.valueOf(e));
        }
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
        AutumnHttpResponse response = handler.handle(request);
        writeResponse(response);
    }

    @Override
    protected void error() throws IOException {
        writeResponse(AutumnHttpResponse.builder()
                .status(400)
                .build()
        );
    }

    @Override
    protected void close() throws IOException {
        cChannel.close();
    }

    private void writeResponse(AutumnHttpResponse response) throws IOException {
        cChannel.write(ByteBuffer.wrap(response.toHeaderBytes()), null, new CompletionHandler<Integer, Object>() {
            @Override
            public void completed(Integer result, Object attachment) {
                if (response.getBody() != null) {
                    cChannel.write(ByteBuffer.wrap(response.getBody()), null, new CompletionHandler<Integer, Object>() {
                        @Override
                        public void completed(Integer result, Object attachment) {
                            tryRead();
                        }

                        @Override
                        public void failed(Throwable exc, Object attachment) {
                            log.warn("Failed to write body: {}", String.valueOf(exc));
                            tryClose();
                        }
                    });
                } else {
                    tryRead();
                }
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                log.warn("Failed to write headers: {}", String.valueOf(exc));
                tryClose();
            }
        });
    }
}
