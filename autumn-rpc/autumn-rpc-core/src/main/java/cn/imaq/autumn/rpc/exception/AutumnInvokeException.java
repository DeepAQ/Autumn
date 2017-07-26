package cn.imaq.autumn.rpc.exception;

public class AutumnInvokeException extends Exception {
    public AutumnInvokeException(String message) {
        super(message);
    }

    public AutumnInvokeException(Throwable cause) {
        super(cause);
    }
}
