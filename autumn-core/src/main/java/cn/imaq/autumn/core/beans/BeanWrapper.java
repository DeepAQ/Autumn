package cn.imaq.autumn.core.beans;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class BeanWrapper {
    @Getter
    private BeanInfo beanInfo;

    @Getter
    @Setter
    private Object beanInstance;
}
