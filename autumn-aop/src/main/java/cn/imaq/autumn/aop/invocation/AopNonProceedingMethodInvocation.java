package cn.imaq.autumn.aop.invocation;

import lombok.AllArgsConstructor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

@AllArgsConstructor
public class AopNonProceedingMethodInvocation implements MethodInvocation {
    private MethodInvocation invocation;

    @Override
    public Object getThis() {
        return invocation.getThis();
    }

    @Override
    public AccessibleObject getStaticPart() {
        return invocation.getStaticPart();
    }

    @Override
    public Method getMethod() {
        return invocation.getMethod();
    }

    @Override
    public Object[] getArguments() {
        return invocation.getArguments();
    }

    @Override
    public Object proceed() {
        throw new UnsupportedOperationException("Calling proceed() is unsupported in non-around advice");
    }
}
