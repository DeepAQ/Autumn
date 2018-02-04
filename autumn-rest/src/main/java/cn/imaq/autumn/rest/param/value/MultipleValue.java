package cn.imaq.autumn.rest.param.value;

import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.Collection;

@AllArgsConstructor
public class MultipleValue<T> implements ParamValue<T> {
    private Collection<T> values;

    public MultipleValue(T[] valueArray) {
        this.values = Arrays.asList(valueArray);
    }

    @Override
    public T getSingleValue() {
        if (values.isEmpty()) {
            return null;
        }
        return values.iterator().next();
    }

    @Override
    public Collection<T> getMultipleValues() {
        return values;
    }
}
