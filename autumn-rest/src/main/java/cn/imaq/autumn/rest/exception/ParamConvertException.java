package cn.imaq.autumn.rest.exception;

public class ParamConvertException extends Exception {
    public ParamConvertException(String message) {
        super(message);
    }

    public ParamConvertException(Throwable cause) {
        super(cause);
    }

    public ParamConvertException(Object src, Class<?> targetType) {
        this("Cannot convert " + src.getClass().getName() + " to " + targetType.getName());
    }
}
