package cn.imaq.autumn.rpc.client.exception;

import java.io.IOException;

public class AutumnHttpException extends IOException {
    private int httpCode;

    public AutumnHttpException(int httpCode) {
        this.httpCode = httpCode;
    }

    @Override
    public String getMessage() {
        return "HTTP request failed with status code " + httpCode;
    }
}
