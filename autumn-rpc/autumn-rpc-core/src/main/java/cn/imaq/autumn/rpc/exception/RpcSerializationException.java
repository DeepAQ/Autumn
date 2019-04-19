package cn.imaq.autumn.rpc.exception;

import java.io.IOException;

public class RpcSerializationException extends IOException {
    public RpcSerializationException(Throwable cause) {
        super(cause);
    }

    public RpcSerializationException(String message) {
        super(message);
    }
}
