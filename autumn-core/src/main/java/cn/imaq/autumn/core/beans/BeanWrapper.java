package cn.imaq.autumn.core.beans;

import cn.imaq.autumn.core.context.AutumnContext;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class BeanWrapper {
    @Getter
    private AutumnContext context;

    @Getter
    private BeanInfo beanInfo;

    @Getter
    @Setter
    private Object beanInstance;
}
