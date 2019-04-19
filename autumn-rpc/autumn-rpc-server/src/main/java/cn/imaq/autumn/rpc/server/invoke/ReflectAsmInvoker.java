package cn.imaq.autumn.rpc.server.invoke;

import cn.imaq.autumn.rpc.serialization.RpcSerialization;
import cn.imaq.autumn.rpc.server.asm.MethodAccess;
import cn.imaq.autumn.rpc.server.exception.RpcInvocationException;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectAsmInvoker implements RpcMethodInvoker {
    private Map<Class, MethodAccess> methodAccessCache = new ConcurrentHashMap<>();
    private Map<RpcMethod, Integer> methodIndexCache = new ConcurrentHashMap<>();

    @Override
    public Object invoke(Object instance, RpcMethod method, Object[] params, RpcSerialization serialization) throws RpcInvocationException, InvocationTargetException {
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
            throw new RpcInvocationException(e);
        } catch (Throwable t) {
            throw new InvocationTargetException(t);
        }
    }
}
