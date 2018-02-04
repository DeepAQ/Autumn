package cn.imaq.autumn.rest.param.value;

import java.util.Collection;

public interface ParamValue<T> {
    T getSingleValue();

    Collection<T> getMultipleValues();
}
