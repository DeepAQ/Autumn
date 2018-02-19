package cn.imaq.autumn.core.beans;

import cn.imaq.autumn.core.beans.creator.BeanCreator;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BeanInfo<T> {
    private String name;

    private Class<T> type;

    private boolean singleton;

    private BeanCreator<T> creator;
}
