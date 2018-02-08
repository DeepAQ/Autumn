package cn.imaq.autumn.rest.exception;

public class MessageConvertException extends Exception {
    public MessageConvertException(String message) {
        super(message);
    }

    public MessageConvertException(Throwable cause) {
        super(cause);
    }
}
