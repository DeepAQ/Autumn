package cn.imaq.autumn.rest.param.resolver;

import java.lang.reflect.ParameterizedType;

public abstract class TypedParamResolver<T> implements ParamResolver {
    @SuppressWarnings("unchecked")
    public Class<T> getType() {
        try {
            return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        } catch (Exception e) {
            return null;
        }
    }
}
