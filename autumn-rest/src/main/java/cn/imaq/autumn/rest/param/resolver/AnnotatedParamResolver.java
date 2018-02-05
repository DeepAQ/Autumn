package cn.imaq.autumn.rest.param.resolver;

import cn.imaq.autumn.rest.param.value.ParamValue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;

public abstract class AnnotatedParamResolver<A extends Annotation> implements ParamResolver {
    @SuppressWarnings("unchecked")
    public Class<A> getAnnotationClass() {
        try {
            return (Class<A>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public ParamValue resolve(Parameter param, HttpServletRequest request, HttpServletResponse response) {
        try {
            A anno = param.getAnnotation(this.getAnnotationClass());
            if (anno == null) {
                return null;
            }
            return resolve(param, anno, request, response);
        } catch (Throwable t) {
            return null;
        }
    }

    protected abstract ParamValue resolve(Parameter param, A anno, HttpServletRequest request, HttpServletResponse response);
}
