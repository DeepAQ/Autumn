package cn.imaq.autumn.rpc.registry.exception;

public class RpcRegistryException extends Exception {
    public RpcRegistryException(String message) {
        super(message);
    }

    public RpcRegistryException(Throwable cause) {
        super(cause);
    }
}
