package cn.imaq.autumn.rpc.client.exception;

import java.io.IOException;

public class RpcHttpException extends IOException {
    private int httpCode;

    public RpcHttpException(int httpCode) {
        this.httpCode = httpCode;
    }

    @Override
    public String getMessage() {
        return "HTTP request failed with status code " + httpCode;
    }
}
