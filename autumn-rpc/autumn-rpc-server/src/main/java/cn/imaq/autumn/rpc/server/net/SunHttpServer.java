package cn.imaq.autumn.rpc.server.net;

import cn.imaq.autumn.rpc.server.exception.AutumnHttpException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

@Slf4j
public class SunHttpServer extends AbstractAutumnHttpServer {
    private HttpServer httpServer;

    public SunHttpServer(String host, int port, AutumnHttpHandler handler) {
        super(host, port, handler);
        try {
            httpServer = HttpServer.create(new InetSocketAddress(host, port), 0);
            httpServer.setExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
            httpServer.createContext("/", new HttpHandler() {
                @Override
                public void handle(HttpExchange req) throws IOException {
                    InputStream is = req.getRequestBody();
                    byte[] buf = new byte[is.available()];
                    is.read(buf);
                    is.close();
                    RPCHttpResponse response = handler.handle(RPCHttpRequest.builder()
                            .method(req.getRequestMethod())
                            .path(req.getRequestURI().getPath())
                            .body(buf)
                            .build()
                    );
                    req.getResponseHeaders().set("Content-Type", response.getContentType());
                    req.sendResponseHeaders(response.getCode(), response.getBody().length);
                    OutputStream os = req.getResponseBody();
                    os.write(response.getBody());
                    os.close();
                }
            });
        } catch (IOException e) {
            log.error("Error creating HTTP server: " + e);
        }
    }

    @Override
    public synchronized void start() throws AutumnHttpException {
        httpServer.start();
    }

    @Override
    public synchronized void stop() {
        httpServer.stop(0);
    }
}
