package cn.imaq.autumn.rest.param.converter;

import cn.imaq.autumn.rest.exception.ParamConvertException;

import java.util.Arrays;
import java.util.List;

public class StringConverter implements TypeConverter {
    @Override
    public List<Class<?>> getTargetTypes() {
        return Arrays.asList(String.class, CharSequence.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T convert(Object src, Class<T> targetType) throws ParamConvertException {
        return (T) String.valueOf(src);
    }
}
