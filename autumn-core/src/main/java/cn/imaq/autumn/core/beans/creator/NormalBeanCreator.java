package cn.imaq.autumn.core.beans.creator;

import cn.imaq.autumn.core.exception.BeanCreationException;

public class NormalBeanCreator implements BeanCreator {
    private Class<?> type;

    public NormalBeanCreator(Class<?> type) {
        this.type = type;
    }

    @Override
    public Object createBean() throws BeanCreationException {
        try {
            return type.newInstance();
        } catch (Exception e) {
            throw new BeanCreationException(e);
        }
    }
}
