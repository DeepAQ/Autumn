package cn.imaq.autumn.rpc.server.exception;

import java.io.IOException;

public class AutumnHttpException extends IOException {
    public AutumnHttpException(String message) {
        super(message);
    }

    public AutumnHttpException(Throwable cause) {
        super(cause);
    }
}
