package cn.imaq.autumn.rpc.exception;

public class RPCInvokeException extends Exception {
    public RPCInvokeException(String message) {
        super(message);
    }

    public RPCInvokeException(Throwable cause) {
        super(cause);
    }
}
