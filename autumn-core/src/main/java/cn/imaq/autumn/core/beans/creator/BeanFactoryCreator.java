package cn.imaq.autumn.core.beans.creator;

import cn.imaq.autumn.core.context.AutumnContext;
import cn.imaq.autumn.core.exception.BeanCreationException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class BeanFactoryCreator implements BeanCreator {
    private AutumnContext context;
    private Method factoryMethod;
    private boolean isStatic;

    public BeanFactoryCreator(AutumnContext context, Method factoryMethod) {
        this.context = context;
        this.factoryMethod = factoryMethod;
        this.isStatic = Modifier.isStatic(factoryMethod.getModifiers());
    }

    @Override
    public Object createBean() throws BeanCreationException {
        try {
            if (isStatic) {
                return factoryMethod.invoke(null);
            } else {
                Class<?> factoryClass = factoryMethod.getDeclaringClass();
                Object factoryInstance = context.getBeanByType(factoryClass);
                if (factoryInstance == null) {
                    throw new BeanCreationException("Cannot get bean for factory " + factoryClass.getName());
                }
                return factoryMethod.invoke(factoryInstance);
            }
        } catch (IllegalAccessException e) {
            throw new BeanCreationException(e);
        } catch (InvocationTargetException e) {
            throw new BeanCreationException(e.getCause());
        }
    }
}
