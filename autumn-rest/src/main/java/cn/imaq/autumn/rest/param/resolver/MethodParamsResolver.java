package cn.imaq.autumn.rest.param.resolver;

import cn.imaq.autumn.rest.exception.ParamConvertException;
import cn.imaq.autumn.rest.exception.ParamResolveException;
import cn.imaq.autumn.rest.param.converter.TypeConverter;
import cn.imaq.autumn.rest.param.value.ParamValue;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MethodParamsResolver {
    private static Map<Class<? extends Annotation>, AnnotatedParamResolver> annotatedResolvers = new HashMap<>();
    private static Map<Class<?>, TypeConverter> typeConverters = new HashMap<>();

    private static void addAnnotatedResolver(AnnotatedParamResolver<?> resolver) {
        annotatedResolvers.put(resolver.getAnnotationClass(), resolver);
    }

    private static void addTypeConverter(TypeConverter<?> converter) {
        for (Class targetType : converter.getTargetTypes()) {
            typeConverters.put(targetType, converter);
        }
    }

    static {
        new FastClasspathScanner()
                .matchSubclassesOf(AnnotatedParamResolver.class, cls -> {
                    try {
                        addAnnotatedResolver(cls.newInstance());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .matchClassesImplementing(TypeConverter.class, cls -> {
                    try {
                        addTypeConverter(cls.newInstance());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .scan();
    }

    public Object[] resolveAll(Method method, HttpServletRequest req, HttpServletResponse resp) throws ParamResolveException {
        Parameter[] params = method.getParameters();
        Object[] rawValues = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            Parameter param = params[i];
            ParamResolver resolver = null;
            // try annotated resolvers first
            for (Annotation annotation : param.getAnnotations()) {
                if (annotatedResolvers.containsKey(annotation.annotationType())) {
                    resolver = annotatedResolvers.get(annotation.annotationType());
                    break;
                }
            }
            if (resolver == null) {
                // TODO try typed resolvers
            }
            if (resolver == null) {
                throw new ParamResolveException("No resolvers found for param " + param);
            }
            // resolve
            ParamValue value = resolver.resolve(param, req, resp);
            Class<?> paramType = param.getType();
            boolean needMultipleValues = paramType.isArray() || Collection.class.isAssignableFrom(paramType);
            Object rawValue = needMultipleValues ? value.getMultipleValues() : value.getSingleValue();
            try {
                if (rawValue != null && !paramType.isAssignableFrom(rawValue.getClass())) {
                    // convert types
                    if (needMultipleValues) {
                        // TODO convert multiple values
                    } else {
                        rawValue = convertSingle(rawValue, paramType);
                    }
                }
            } catch (ParamConvertException e) {
                throw new ParamResolveException(e);
            }
            rawValues[i] = rawValue;
        }
        return rawValues;
    }

    private <T> T convertSingle(Object src, Class<T> targetType) throws ParamConvertException {
        TypeConverter<T> converter = typeConverters.get(targetType);
        if (converter == null) {
            throw new ParamConvertException("Unable to find converter for " + src.getClass().getName() + " to " + targetType.getName());
        }
        return converter.convert(src, targetType);
    }
}
