package cn.imaq.autumn.rpc.server.invoker;

import cn.imaq.autumn.rpc.exception.AutumnInvokeException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectionInvoker implements AutumnInvoker {
    private Map<AutumnMethod, Method> methodCache = new ConcurrentHashMap<>();

    @Override
    public Object invoke(Object instance, AutumnMethod method, Object[] params) throws AutumnInvokeException, InvocationTargetException {
        try {
            Method realMethod = methodCache.get(method);
            if (realMethod == null) {
                realMethod = instance.getClass().getMethod(method.getName(), method.getParamTypes());
                methodCache.put(method, realMethod);
            }
            return realMethod.invoke(instance, params);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new AutumnInvokeException(e);
        }
    }
}
