package cn.imaq.autumn.rest.param.converter;

import cn.imaq.autumn.rest.exception.ParamConvertException;

import java.util.List;

public interface TypeConverter<T> {
    List<Class<? extends T>> getTargetTypes();

    <P extends T> P convert(Object src, Class<P> targetType) throws ParamConvertException;
}
