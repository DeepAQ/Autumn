package cn.imaq.autumn.rest.param.converter;

import cn.imaq.autumn.rest.exception.ParamConvertException;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

public class NumberConverter implements TypeConverter<Number> {
    @Override
    public List<Class<? extends Number>> getTargetTypes() {
        return Arrays.asList(Number.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, byte.class, short.class, int.class, long.class, float.class, double.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <P extends Number> P convert(Object src, Class<P> targetType) throws ParamConvertException {
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
            return (P) Byte.valueOf(number.byteValue());
        } else if (Short.class == targetType || short.class == targetType) {
            return (P) Short.valueOf(number.shortValue());
        } else if (Integer.class == targetType || int.class == targetType) {
            return (P) Integer.valueOf(number.intValue());
        } else if (Long.class == targetType || long.class == targetType) {
            return (P) Long.valueOf(number.longValue());
        } else if (Float.class == targetType || float.class == targetType) {
            return (P) Float.valueOf(number.floatValue());
        } else if (Double.class == targetType || double.class == targetType) {
            return (P) Double.valueOf(number.doubleValue());
        }
        throw new ParamConvertException(src, targetType);
    }
}
