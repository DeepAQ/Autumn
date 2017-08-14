package cn.imaq.autumn.rpc.server.invoker;

import cn.imaq.autumn.rpc.exception.AutumnInvokeException;
import cn.imaq.autumn.rpc.exception.AutumnSerializationException;
import cn.imaq.autumn.rpc.serialization.AutumnSerialization;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectionInvoker implements AutumnInvoker {
    private Map<AutumnMethod, Method> methodCache = new ConcurrentHashMap<>();

    @Override
    public Object invoke(Object instance, AutumnMethod method, Object[] params, AutumnSerialization serialization) throws AutumnInvokeException, InvocationTargetException {
        try {
            Method realMethod = methodCache.get(method);
            if (realMethod == null) {
                if (method.getParamTypes() != null) {
                    realMethod = instance.getClass().getMethod(method.getName(), method.getParamTypes());
                } else {
                    for (Method m : instance.getClass().getMethods()) {
                        if (m.getName().equals(method.getName()) && m.getParameterCount() == method.getParamCount()) {
                            realMethod = m;
                            break;
                        }
                    }
                    if (realMethod == null) {
                        throw new NoSuchMethodException();
                    }
                }
                methodCache.put(method, realMethod);
            }
            params = serialization.convertTypes(params, realMethod.getGenericParameterTypes());
            return realMethod.invoke(instance, params);
        } catch (NoSuchMethodException | IllegalAccessException | AutumnSerializationException e) {
            throw new AutumnInvokeException(e);
        }
    }
}
