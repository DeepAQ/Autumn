package cn.imaq.autumn.rest.param.converter;

import cn.imaq.autumn.rest.exception.ParamConvertException;

import java.util.Collections;
import java.util.List;

public class StringConverter implements TypeConverter<String> {
    @Override
    public List<Class<? extends String>> getTargetTypes() {
        return Collections.singletonList(String.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <P extends String> P convert(Object src, Class<P> targetType) throws ParamConvertException {
        return (P) String.valueOf(src);
    }
}
