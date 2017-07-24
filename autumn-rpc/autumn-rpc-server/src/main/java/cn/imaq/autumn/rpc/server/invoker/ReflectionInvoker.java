package cn.imaq.autumn.rpc.server.invoker;

import cn.imaq.autumn.rpc.server.exception.AutumnInvokeException;

import java.lang.reflect.InvocationTargetException;

public class ReflectionInvoker implements AutumnInvoker {
    @Override
    public Object invoke(Object instance, String methodName, Class[] paramTypes, Object[] params) throws AutumnInvokeException, InvocationTargetException {
        try {
            return instance.getClass().getMethod(methodName, paramTypes).invoke(instance, params);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new AutumnInvokeException(e);
        }
    }
}
