package cn.imaq.autumn.core.beans.creator;

import cn.imaq.autumn.core.exception.BeanCreationException;

public class NormalBeanCreator<T> implements BeanCreator<T> {
    private Class<T> type;

    public NormalBeanCreator(Class<T> type) {
        this.type = type;
    }

    @Override
    public T createBean() throws BeanCreationException {
        try {
            return type.newInstance();
        } catch (Exception e) {
            throw new BeanCreationException(e);
        }
    }
}
