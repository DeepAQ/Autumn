package cn.imaq.autumn.rest.param.resolver;

import cn.imaq.autumn.rest.exception.MethodParamResolveException;
import cn.imaq.autumn.rest.param.resolver.annotated.RequestParamResolver;
import cn.imaq.autumn.rest.param.value.ParamValue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MethodParamsResolver {
    private static Map<Class<? extends Annotation>, AnnotatedParamResolver> annotatedParamResolvers = new ConcurrentHashMap<>();

    private static <T extends Annotation> void addAnnotatedParamResolver(AnnotatedParamResolver<T> resolver) {
        annotatedParamResolvers.put(resolver.getAnnotationClass(), resolver);
    }

    static {
        addAnnotatedParamResolver(new RequestParamResolver());
    }

    public static Object[] resolveAll(Method method, HttpServletRequest req, HttpServletResponse resp) throws MethodParamResolveException {
        Parameter[] params = method.getParameters();
        Object[] paramValues = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            Parameter param = params[i];
            ParamResolver resolver = null;
            // try annotated resolvers first
            for (Annotation annotation : param.getAnnotations()) {
                if (annotatedParamResolvers.containsKey(annotation.annotationType())) {
                    resolver = annotatedParamResolvers.get(annotation.annotationType());
                    break;
                }
            }
            if (resolver == null) {
                // TODO try typed resolvers
            }
            if (resolver == null) {
                throw new MethodParamResolveException("No resolvers found for param " + param);
            }
            // resolve
            ParamValue value = resolver.resolve(param, req, resp);
            // TODO convert object types
            Class<?> paramType = param.getType();
            boolean needMultipleValues = paramType.isArray() || Collection.class.isAssignableFrom(paramType);
            if (needMultipleValues) {
                paramValues[i] = value.getMultipleValues();
            } else {
                paramValues[i] = value.getSingleValue();
            }
        }
        return paramValues;
    }
}
