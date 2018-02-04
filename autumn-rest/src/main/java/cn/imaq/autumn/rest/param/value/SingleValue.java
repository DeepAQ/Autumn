package cn.imaq.autumn.rest.param.value;

import lombok.AllArgsConstructor;

import java.util.Collection;
import java.util.Collections;

@AllArgsConstructor
public class SingleValue<T> implements ParamValue<T> {
    private T value;

    @Override
    public T getSingleValue() {
        return value;
    }

    @Override
    public Collection<T> getMultipleValues() {
        return value == null ? null : Collections.singleton(value);
    }
}
