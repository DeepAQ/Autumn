package cn.imaq.autumn.rpc.server.invoke;

import cn.imaq.autumn.rpc.exception.RPCInvokeException;
import cn.imaq.autumn.rpc.exception.RPCSerializationException;
import cn.imaq.autumn.rpc.serialization.RPCSerialization;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectionInvoker implements RPCMethodInvoker {
    private Map<RPCMethod, Method> methodCache = new ConcurrentHashMap<>();

    @Override
    public Object invoke(Object instance, RPCMethod method, Object[] params, RPCSerialization serialization) throws RPCInvokeException, InvocationTargetException {
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
        } catch (NoSuchMethodException | IllegalAccessException | RPCSerializationException e) {
            throw new RPCInvokeException(e);
        }
    }
}
