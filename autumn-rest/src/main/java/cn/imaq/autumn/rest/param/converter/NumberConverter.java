package cn.imaq.autumn.rest.param.converter;

import cn.imaq.autumn.rest.exception.ParamConvertException;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

public class NumberConverter implements TypeConverter {
    @Override
    public List<Class<?>> getTargetTypes() {
        return Arrays.asList(Number.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, byte.class, short.class, int.class, long.class, float.class, double.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T convert(Object src, Class<T> targetType) throws ParamConvertException {
        Number number;
        if (src instanceof Number) {
            number = ((Number) src);
        } else if (src instanceof String) {
            try {
                number = NumberFormat.getInstance().parse((String) src);
            } catch (ParseException e) {
                throw new ParamConvertException(e);
            }
        } else {
            throw new ParamConvertException(src, targetType);
        }
        if (Byte.class == targetType || byte.class == targetType) {
            return (T) Byte.valueOf(number.byteValue());
        } else if (Short.class == targetType || short.class == targetType) {
            return (T) Short.valueOf(number.shortValue());
        } else if (Integer.class == targetType || int.class == targetType) {
            return (T) Integer.valueOf(number.intValue());
        } else if (Long.class == targetType || long.class == targetType) {
            return (T) Long.valueOf(number.longValue());
        } else if (Float.class == targetType || float.class == targetType) {
            return (T) Float.valueOf(number.floatValue());
        } else if (Double.class == targetType || double.class == targetType) {
            return (T) Double.valueOf(number.doubleValue());
        }
        throw new ParamConvertException(src, targetType);
    }
}
