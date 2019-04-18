package cn.imaq.autumn.rpc.server.invoke;

import cn.imaq.autumn.rpc.exception.RPCInvokeException;
import cn.imaq.autumn.rpc.serialization.RPCSerialization;
import cn.imaq.autumn.rpc.server.asm.MethodAccess;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectAsmInvoker implements RPCMethodInvoker {
    private Map<Class, MethodAccess> methodAccessCache = new ConcurrentHashMap<>();
    private Map<RPCMethod, Integer> methodIndexCache = new ConcurrentHashMap<>();

    @Override
    public Object invoke(Object instance, RPCMethod method, Object[] params, RPCSerialization serialization) throws RPCInvokeException, InvocationTargetException {
        try {
            MethodAccess ma = methodAccessCache.computeIfAbsent(instance.getClass(), k -> MethodAccess.get(instance.getClass()));
            Integer methodIndex = methodIndexCache.get(method);
            if (methodIndex == null) {
                if (method.getParamTypes() != null) {
                    methodIndex = ma.getIndex(method.getName(), method.getParamTypes());
                } else {
                    methodIndex = ma.getIndex(method.getName(), method.getParamCount());
                }
                methodIndexCache.put(method, methodIndex);
            }
            params = serialization.convertTypes(params, ma.getGenericTypes()[methodIndex]);
            return ma.invoke(instance, methodIndex, params);
        } catch (IllegalArgumentException e) {
            throw new RPCInvokeException(e);
        } catch (Throwable t) {
            throw new InvocationTargetException(t);
        }
    }
}
