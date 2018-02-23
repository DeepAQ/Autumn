package cn.imaq.autumn.aop;

import cn.imaq.autumn.core.context.AutumnContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

public class HookChain {
    private Iterator<HookModel> hookItr;

    private AutumnContext context;

    private Object target;

    private Method realMethod;

    private Object[] args;

    public HookChain(Iterator<HookModel> hookItr, AutumnContext context, Object target, Method realMethod, Object[] args) {
        this.hookItr = hookItr;
        this.context = context;
        this.target = target;
        this.realMethod = realMethod;
        this.args = args;
    }

    public Object proceed() throws Throwable {
        try {
            if (hookItr.hasNext()) {
                return invokeHook(hookItr.next().getHook());
            }
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
