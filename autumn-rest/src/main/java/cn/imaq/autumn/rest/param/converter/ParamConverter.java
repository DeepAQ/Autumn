package cn.imaq.autumn.rest.param.converter;

import cn.imaq.autumn.rest.exception.ParamConvertException;

import java.util.List;

public interface ParamConverter {
    List<Class<?>> getTargetTypes();

    <T> T convert(Object src, Class<T> targetType) throws ParamConvertException;
}
