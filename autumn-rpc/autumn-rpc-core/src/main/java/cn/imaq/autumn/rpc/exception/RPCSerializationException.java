package cn.imaq.autumn.rpc.exception;

import java.io.IOException;

public class RPCSerializationException extends IOException {
    public RPCSerializationException(Throwable cause) {
        super(cause);
    }

    public RPCSerializationException(String message) {
        super(message);
    }
}
