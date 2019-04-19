package cn.imaq.autumn.rpc.server.invoke;

import cn.imaq.autumn.rpc.exception.RpcSerializationException;
import cn.imaq.autumn.rpc.serialization.RpcSerialization;
import cn.imaq.autumn.rpc.server.exception.RpcInvocationException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MethodHandleInvoker implements RpcMethodInvoker {
    private Map<RpcMethod, MethodHandle> handleCache = new ConcurrentHashMap<>();
    private Map<RpcMethod, Type[]> typeCache = new ConcurrentHashMap<>();

    @Override
    public Object invoke(Object instance, RpcMethod method, Object[] params, RpcSerialization serialization) throws RpcInvocationException, InvocationTargetException {
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
        } catch (NoSuchMethodException | IllegalAccessException | RpcSerializationException e) {
            throw new RpcInvocationException(e);
        } catch (Throwable t) {
            throw new InvocationTargetException(t);
        }
    }
}
