package cn.imaq.autumn.rpc.server.invoker;

import cn.imaq.autumn.rpc.server.exception.AutumnInvokeException;
import com.esotericsoftware.reflectasm.MethodAccess;

import java.lang.reflect.InvocationTargetException;

public class ReflectAsmInvoker implements AutumnInvoker {
    @Override
    public Object invoke(Object instance, String methodName, Class[] paramTypes, Object[] params) throws AutumnInvokeException, InvocationTargetException {
        try {
            return MethodAccess.get(instance.getClass()).invoke(instance, methodName, paramTypes, params);
        } catch (IllegalArgumentException e) {
            throw new AutumnInvokeException(e);
        } catch (Throwable t) {
            throw new InvocationTargetException(t);
        }
    }
}
