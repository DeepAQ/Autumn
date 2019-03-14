package cn.imaq.autumn.rest.param.resolver;

import cn.imaq.autumn.cpscan.AutumnClasspathScan;
import cn.imaq.autumn.cpscan.ScanResult;
import cn.imaq.autumn.rest.annotation.param.JSON;
import cn.imaq.autumn.rest.exception.ParamConvertException;
import cn.imaq.autumn.rest.exception.ParamResolveException;
import cn.imaq.autumn.rest.param.converter.CollectionConverter;
import cn.imaq.autumn.rest.param.converter.ParamConverter;
import cn.imaq.autumn.rest.param.value.ParamValue;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

@Slf4j
public class MethodParamsResolver {
    private static Map<Class<? extends Annotation>, AnnotatedParamResolver> annotatedResolvers = new HashMap<>();
    private static List<TypedParamResolver> typedResolvers = new ArrayList<>();
    private static Map<Class<?>, ParamConverter> typeConverters = new HashMap<>();
    private static CollectionConverter collectionConverter = new CollectionConverter();
    private static volatile boolean init = false;

    private ObjectMapper jsonMapper = new ObjectMapper();

    private static void ensureInit() {
        if (!init) {
            synchronized (MethodParamsResolver.class) {
                if (!init) {
                    log.info("Initializing param resolvers and converters ...");
                    ScanResult result = AutumnClasspathScan.getGlobalScanResult();
                    result.getSubClassesOf(AnnotatedParamResolver.class).forEach(c -> {
                        try {
                            AnnotatedParamResolver<?> resolver = (AnnotatedParamResolver<?>) c.newInstance();
                            Class<? extends Annotation> annotationClass = resolver.getAnnotationClass();
                            if (annotationClass != null) {
                                annotatedResolvers.put(resolver.getAnnotationClass(), resolver);
                            }
                        } catch (Exception ignored) {
                        }
                    });
                    result.getSubClassesOf(TypedParamResolver.class).forEach(c -> {
                        try {
                            typedResolvers.add((TypedParamResolver<?>) c.newInstance());
                        } catch (Exception ignored) {
                        }
                    });
                    result.getClassesImplementing(ParamConverter.class).forEach(c -> {
                        try {
                            ParamConverter converter = (ParamConverter) c.newInstance();
                            for (Class targetType : converter.getTargetTypes()) {
                                typeConverters.put(targetType, converter);
                            }
                        } catch (Exception ignored) {
                        }
                    });
                    init = true;
                }
            }
        }
    }

    public Object[] resolveAll(Method method, HttpServletRequest req, HttpServletResponse resp) throws ParamResolveException {
        return resolveAllWithThrowable(method, req, resp, null);
    }

    public Object[] resolveAllWithThrowable(Method method, HttpServletRequest req, HttpServletResponse resp, Throwable throwable) throws ParamResolveException {
        ensureInit();
        Parameter[] params = method.getParameters();
        Object[] rawValues = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            Parameter param = params[i];
            // process throwable
            if (throwable != null && param.getType().isInstance(throwable)) {
                rawValues[i] = throwable;
                continue;
            }
            // look for suitable resolver
            ParamResolver resolver = null;
            // try annotated resolvers first
            for (Class<? extends Annotation> annoClass : annotatedResolvers.keySet()) {
                if (param.isAnnotationPresent(annoClass)) {
                    resolver = annotatedResolvers.get(annoClass);
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
                try {
                    return convertFromJson(param, value);
                } catch (ParamConvertException e1) {
                    throw new ParamConvertException("Cannot convert param " + param + ", tried all converters");
                }
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
        ParamConverter converter = typeConverters.get(targetType);
        if (converter == null) {
            throw new ParamConvertException("Unable to find converter to " + targetType.getName());
        }
        return converter.convert(src, targetType);
    }

    private Object convertMultiple(Collection<?> src, Class<?> targetType) throws ParamConvertException {
        ParamConverter converter = null;
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
