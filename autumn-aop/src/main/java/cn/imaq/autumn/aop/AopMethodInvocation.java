package cn.imaq.autumn.aop;

import cn.imaq.autumn.aop.advice.Advice;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

@AllArgsConstructor
public class AopMethodInvocation implements MethodInvocation {
    private Iterator<Advice> adviceItr;

    @Getter
    private Object target;

    private Method realMethod;

    private Object[] args;

    @Override
    public Object getThis() {
        return this;
    }

    @Override
    public AccessibleObject getStaticPart() {
        return this.realMethod;
    }

    @Override
    public Method getMethod() {
        return this.realMethod;
    }

    @Override
    public Object[] getArguments() {
        return this.args;
    }

    @Override
    public Object proceed() throws Throwable {
        if (adviceItr.hasNext()) {
            return adviceItr.next().invoke(this);
        }

        try {
            return realMethod.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
