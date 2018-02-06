package cn.imaq.autumn.rest.param.resolver;

import cn.imaq.autumn.rest.annotation.param.JSON;
import cn.imaq.autumn.rest.exception.ParamConvertException;
import cn.imaq.autumn.rest.exception.ParamResolveException;
import cn.imaq.autumn.rest.param.converter.CollectionConverter;
import cn.imaq.autumn.rest.param.converter.TypeConverter;
import cn.imaq.autumn.rest.param.value.ParamValue;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MethodParamsResolver {
    private static Map<Class<? extends Annotation>, AnnotatedParamResolver> annotatedResolvers = new HashMap<>();
    private static List<TypedParamResolver> typedResolvers = new ArrayList<>();
    private static Map<Class<?>, TypeConverter> typeConverters = new HashMap<>();
    private static CollectionConverter collectionConverter = new CollectionConverter();

    static {
        log.info("Initializing param resolvers and converters ...");
        new FastClasspathScanner()
                .matchSubclassesOf(AnnotatedParamResolver.class, cls -> {
                    try {
                        AnnotatedParamResolver<?> resolver = cls.newInstance();
                        annotatedResolvers.put(resolver.getAnnotationClass(), resolver);
                    } catch (Exception ignored) {
                    }
                })
                .matchSubclassesOf(TypedParamResolver.class, cls -> {
                    try {
                        typedResolvers.add(cls.newInstance());
                    } catch (Exception ignored) {
                    }
                })
                .matchClassesImplementing(TypeConverter.class, cls -> {
                    try {
                        TypeConverter converter = cls.newInstance();
                        for (Class targetType : converter.getTargetTypes()) {
                            typeConverters.put(targetType, converter);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .scan();
    }

    private Map<Parameter, ParamResolver> resolverCache = new ConcurrentHashMap<>();
    private ObjectMapper jsonMapper = new ObjectMapper();

    public Object[] resolveAll(Method method, HttpServletRequest req, HttpServletResponse resp) throws ParamResolveException {
        Parameter[] params = method.getParameters();
        Object[] rawValues = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            Parameter param = params[i];
            // look for suitable resolver
            ParamResolver resolver = resolverCache.get(param);
            if (resolver == null) {
                // try annotated resolvers first
                for (Annotation annotation : param.getAnnotations()) {
                    if (annotatedResolvers.containsKey(annotation.annotationType())) {
                        resolver = annotatedResolvers.get(annotation.annotationType());
                        break;
                    }
                }
                if (resolver == null) {
                    Class<?> paramType = param.getType();
                    for (TypedParamResolver typedResolver : typedResolvers) {
                        if (paramType.isAssignableFrom(typedResolver.getType())) {
                            resolver = typedResolver;
                            break;
                        }
                    }
                }
                if (resolver == null) {
                    throw new ParamResolveException("No suitable resolvers found for param " + param);
                }
                resolverCache.put(param, resolver);
            }
            // resolve parameter
            ParamValue value = resolver.resolve(param, req, resp);
            if (value == null) {
                throw new ParamResolveException("No suitable resolvers found for param " + param);
            }
            Object rawValue;
            // process JSON annotation
            try {
                if (param.isAnnotationPresent(JSON.class)) {
                    rawValue = convertFromJson(param, value);
                } else {
                    // convert type
                    rawValue = convertParam(param, value);
                }
            } catch (ParamConvertException e) {
                throw new ParamResolveException(e);
            }
            rawValues[i] = rawValue;
        }
        return rawValues;
    }

    private Object convertParam(Parameter param, ParamValue value) throws ParamConvertException {
        Class<?> paramType = param.getType();
        if (paramType.isInstance(value.getSingleValue())) {
            return value.getSingleValue();
        }
        if (paramType.isInstance(value.getMultipleValues())) {
            return value.getMultipleValues();
        }
        boolean needMultipleValues = paramType.isArray() || Collection.class.isAssignableFrom(paramType);
        Object rawValue = needMultipleValues ? value.getMultipleValues() : value.getSingleValue();
        if (rawValue != null) {
            try {
                // convert types
                if (needMultipleValues) {
                    Collection valueCollection = ((Collection) rawValue);
                    if (paramType.isArray()) {
                        Class<?> innerType = paramType.getComponentType();
                        return convertMultiple(valueCollection, innerType);
                    } else {
                        Type type = param.getParameterizedType();
                        if (type instanceof ParameterizedType) {
                            Type innerType = ((ParameterizedType) type).getActualTypeArguments()[0];
                            if (innerType instanceof Class) {
                                valueCollection = Arrays.asList((Object[]) convertMultiple(valueCollection, (Class<?>) innerType));
                            }
                        }
                        // convert collections types (if needed)
                        return collectionConverter.convert(valueCollection, (Class<?>) paramType);
                    }
                } else {
                    return convertSingle(rawValue, paramType);
                }
            } catch (ParamConvertException e) {
                // try JSON
                return convertFromJson(param, value);
            }
        }
        return null;
    }

    private Object convertFromJson(Parameter param, ParamValue value) throws ParamConvertException {
        Object singleValue = value.getSingleValue();
        try {
            if (singleValue instanceof String) {
                return jsonMapper.readValue((String) singleValue,
                        jsonMapper.constructType(param.getParameterizedType()));
            } else if (singleValue instanceof byte[]) {
                return jsonMapper.readValue(((byte[]) singleValue),
                        jsonMapper.constructType(param.getParameterizedType()));
            } else if (singleValue instanceof TreeNode) {
                return jsonMapper.convertValue(singleValue,
                        jsonMapper.constructType(param.getParameterizedType()));
            }
        } catch (IOException e) {
            throw new ParamConvertException(e);
        }
        throw new ParamConvertException("Param cannot be converted as JSON");
    }

    private <T> T convertSingle(Object src, Class<T> targetType) throws ParamConvertException {
        TypeConverter converter = typeConverters.get(targetType);
        if (converter == null) {
            throw new ParamConvertException("Unable to find converter to " + targetType.getName());
        }
        return converter.convert(src, targetType);
    }

    private Object convertMultiple(Collection<?> src, Class<?> targetType) throws ParamConvertException {
        TypeConverter converter = null;
        // List<T> results = new ArrayList<>(src.size());
        Object results = Array.newInstance(targetType, src.size());
        int index = 0;
        for (Object o : src) {
            if (targetType.isInstance(o)) {
                Array.set(results, index, o);
            } else {
                if (converter == null) {
                    converter = typeConverters.get(targetType);
                }
                if (converter == null) {
                    throw new ParamConvertException("Unable to find converter to " + targetType.getName());
                }
                Array.set(results, index, converter.convert(o, targetType));
            }
            index++;
        }
        return results;
    }
}
