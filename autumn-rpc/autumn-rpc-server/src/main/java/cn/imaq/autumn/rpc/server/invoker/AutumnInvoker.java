package cn.imaq.autumn.rpc.server.invoker;

import cn.imaq.autumn.rpc.server.exception.AutumnInvokeException;

import java.lang.reflect.InvocationTargetException;

public interface AutumnInvoker {
    Object invoke(Object instance, AutumnMethod method, Object[] params) throws AutumnInvokeException, InvocationTargetException;
}