package cn.imaq.autumn.rest.exception;

public class ParamResolveException extends Exception {
    public ParamResolveException(String message) {
        super(message);
    }

    public ParamResolveException(Throwable cause) {
        super(cause);
    }
}
