package cn.imaq.autumn.core.exception;

public class BeanCreationException extends Exception {
    public BeanCreationException(String message) {
        super(message);
    }

    public BeanCreationException(Throwable cause) {
        super(cause);
    }
}
