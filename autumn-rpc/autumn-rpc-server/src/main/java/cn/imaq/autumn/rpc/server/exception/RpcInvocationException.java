package cn.imaq.autumn.rpc.server.exception;

public class RpcInvocationException extends Exception {
    public RpcInvocationException(String message) {
        super(message);
    }

    public RpcInvocationException(Throwable cause) {
        super(cause);
    }
}
