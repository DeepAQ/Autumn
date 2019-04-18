package cn.imaq.autumn.rpc.server.invoke;

import cn.imaq.autumn.rpc.exception.RPCInvokeException;
import cn.imaq.autumn.rpc.exception.RPCSerializationException;
import cn.imaq.autumn.rpc.serialization.RPCSerialization;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MethodHandleInvoker implements RPCMethodInvoker {
    private Map<RPCMethod, MethodHandle> handleCache = new ConcurrentHashMap<>();
    private Map<RPCMethod, Type[]> typeCache = new ConcurrentHashMap<>();

    @Override
    public Object invoke(Object instance, RPCMethod method, Object[] params, RPCSerialization serialization) throws RPCInvokeException, InvocationTargetException {
        try {
            MethodHandle handle = handleCache.get(method);
            Type[] paramTypes = typeCache.get(method);
            if (handle == null || paramTypes == null) {
                Method realMethod = null;
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
                handle = MethodHandles.lookup().unreflect(realMethod);
                paramTypes = realMethod.getGenericParameterTypes();
                handleCache.put(method, handle);
                typeCache.put(method, paramTypes);
            }
            params = serialization.convertTypes(params, paramTypes);
            Object[] newParams = new Object[params.length + 1];
            newParams[0] = instance;
            System.arraycopy(params, 0, newParams, 1, params.length);
            return handle.invokeWithArguments(newParams);
        } catch (NoSuchMethodException | IllegalAccessException | RPCSerializationException e) {
            throw new RPCInvokeException(e);
        } catch (Throwable t) {
            throw new InvocationTargetException(t);
        }
    }
}
