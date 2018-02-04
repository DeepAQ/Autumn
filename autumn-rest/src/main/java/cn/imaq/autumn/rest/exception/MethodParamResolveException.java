package cn.imaq.autumn.rest.exception;

public class MethodParamResolveException extends Exception {
    public MethodParamResolveException(String message) {
        super(message);
    }

    public MethodParamResolveException(String message, Throwable cause) {
        super(message, cause);
    }
}
