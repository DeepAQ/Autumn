package cn.imaq.autumn.rest.message;

import cn.imaq.autumn.rest.exception.MessageConvertException;

import java.lang.reflect.Type;

public class DefaultConverterDelegate implements MessageConverter {
    private MessageConverter delegate = new JacksonMessageConverter();

    @Override
    public String getContentType() {
        return delegate.getContentType();
    }

    @Override
    public byte[] toBytes(Object src) throws MessageConvertException {
        return delegate.toBytes(src);
    }

    @Override
    public Object fromBytes(byte[] src, Type type) throws MessageConvertException {
        return delegate.fromBytes(src, type);
    }
}
