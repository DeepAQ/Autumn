package cn.imaq.autumn.http.client.protocol;

import cn.imaq.autumn.http.protocol.AbstractHttpSession;
import cn.imaq.autumn.http.protocol.AutumnHttpResponse;

import java.io.IOException;

public class HttpClientSession extends AbstractHttpSession {
    private int responseCode;
    private String protocol;
    private boolean finished = false;

    public boolean isFinished() {
        return finished;
    }

    public AutumnHttpResponse getResponse() {
        String contentType = headersMap != null ? headersMap.get("content-type").get(0) : null;
        return AutumnHttpResponse.builder()
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
            responseCode = Integer.parseInt(words[1]);
            return true;
        }
        return false;
    }

    @Override
    protected void finish() throws IOException {
        finished = true;
    }

    @Override
    protected void error() throws IOException {
        System.err.println("Error parsing HTTP response");
    }
}
