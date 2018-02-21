package cn.imaq.autumn.core.exception;

public class BeanPopulationException extends Exception {
    public BeanPopulationException(String message) {
        super(message);
    }

    public BeanPopulationException(Throwable cause) {
        super(cause);
    }
}
