package cn.imaq.autumn.rpc.server.invoker;

import cn.imaq.autumn.rpc.exception.AutumnInvokeException;
import cn.imaq.autumn.rpc.serialization.AutumnSerialization;
import cn.imaq.autumn.rpc.server.asm.MethodAccess;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectAsmInvoker implements AutumnInvoker {
    private Map<Class, MethodAccess> methodAccessCache = new ConcurrentHashMap<>();
    private Map<AutumnMethod, Integer> methodIndexCache = new ConcurrentHashMap<>();

    @Override
    public Object invoke(Object instance, AutumnMethod method, Object[] params, AutumnSerialization serialization) throws AutumnInvokeException, InvocationTargetException {
        try {
            MethodAccess ma = methodAccessCache.computeIfAbsent(instance.getClass(), k -> MethodAccess.get(instance.getClass()));
            Integer methodIndex = methodIndexCache.get(method);
            //noinspection Java8MapApi
            if (methodIndex == null) {
                methodIndex = ma.getIndex(method.getName(), method.getParamTypes());
                methodIndexCache.put(method, methodIndex);
            }
            params = serialization.convertTypes(params, ma.getGenericTypes()[methodIndex]);
            return ma.invoke(instance, methodIndex, params);
        } catch (IllegalArgumentException e) {
            throw new AutumnInvokeException(e);
        } catch (Throwable t) {
            throw new InvocationTargetException(t);
        }
    }
}
