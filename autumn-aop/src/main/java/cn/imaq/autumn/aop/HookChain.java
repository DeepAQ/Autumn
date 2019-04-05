package cn.imaq.autumn.aop;

import cn.imaq.autumn.core.context.AutumnContext;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

@AllArgsConstructor
public class HookChain implements MethodInvocation {
    private Iterator<HookModel> hookItr;

    @Getter
    private AutumnContext context;

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
        if (hookItr.hasNext()) {
            return invokeHook(hookItr.next().getHook());
        }

        try {
            return realMethod.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private Object invokeHook(Method hook) throws InvocationTargetException, IllegalAccessException {
        if (hook.getParameterCount() > 0) {
            return hook.invoke(context.getBeanByType(hook.getDeclaringClass()), this);
        } else {
            hook.invoke(context.getBeanByType(hook.getDeclaringClass()));
            return realMethod.invoke(target, args);
        }
    }
}
