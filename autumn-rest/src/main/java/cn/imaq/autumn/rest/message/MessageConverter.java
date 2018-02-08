package cn.imaq.autumn.rest.message;

import cn.imaq.autumn.rest.exception.MessageConvertException;

import java.lang.reflect.Type;

public interface MessageConverter {
    String getContentType();

    byte[] toBytes(Object src) throws MessageConvertException;

    Object fromBytes(byte[] src, Type type) throws MessageConvertException;
}
