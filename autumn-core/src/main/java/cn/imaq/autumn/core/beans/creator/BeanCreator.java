package cn.imaq.autumn.core.beans.creator;

import cn.imaq.autumn.core.exception.BeanCreationException;

public interface BeanCreator<T> {
    T createBean() throws BeanCreationException;
}
