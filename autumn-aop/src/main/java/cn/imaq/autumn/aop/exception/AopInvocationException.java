package cn.imaq.autumn.aop.exception;

public class AopInvocationException extends Exception {
    public AopInvocationException(String message) {
        super(message);
    }

    public AopInvocationException(Throwable cause) {
        super(cause);
    }
}
