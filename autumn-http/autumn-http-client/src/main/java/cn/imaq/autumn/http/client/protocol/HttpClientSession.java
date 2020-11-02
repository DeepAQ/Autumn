package cn.imaq.autumn.http.client.protocol;

import cn.imaq.autumn.http.protocol.AbstractHttpSession;
import cn.imaq.autumn.http.protocol.AutumnHttpResponse;

import java.io.IOException;

public class HttpClientSession extends AbstractHttpSession {
    private int responseCode;
    private String protocol;
    private boolean finished = false;

    public HttpClientSession() {
        super(1024 * 1024);
    }

    public boolean isFinished() {
        return finished;
    }

    public AutumnHttpResponse getResponse() {
        String contentType = headersMap != null ? headersMap.get("content-type").get(0) : null;
        return AutumnHttpResponse.builder()
                .protocol(protocol)
                .status(responseCode)
                .headers(headersMap)
                .contentType(contentType)
                .body(body)
                .build();
    }

    @Override
    protected boolean checkStart(String line) {
        // expect: "HTTP/1.1 418 I'm a teapot"
        String[] words = line.split(" ", 3);
        if (words.length == 3 && words[0].startsWith("HTTP")) {
            protocol = words[0];
            try {
                responseCode = Integer.parseInt(words[1]);
            } catch (NumberFormatException e) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    protected void finish() {
        finished = true;
    }

    @Override
    protected void error() {
        System.err.println("Error parsing HTTP response");
    }

    @Override
    protected void close() throws IOException {
        // TODO
    }
}
