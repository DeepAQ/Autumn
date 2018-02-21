package cn.imaq.autumn.core.beans.populator;

import cn.imaq.autumn.core.context.AutumnContext;
import cn.imaq.autumn.core.exception.BeanPopulationException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

public abstract class AnnotatedFieldPopulator<A extends Annotation> {
    @SuppressWarnings("unchecked")
    public Class<A> getAnnotationClass() {
        try {
            return (Class<A>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        } catch (Exception e) {
            return null;
        }
    }

    public Object populate(AutumnContext context, Field field) throws BeanPopulationException {
        Class<A> annoClass = getAnnotationClass();
        Object result = null;
        if (field.isAnnotationPresent(annoClass)) {
            result = populate(context, field, field.getAnnotation(annoClass));
        }
        return result;
    }

    protected abstract Object populate(AutumnContext context, Field field, A anno) throws BeanPopulationException;
}
