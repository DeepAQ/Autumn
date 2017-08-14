package cn.imaq.autumn.rpc.server.invoker;

import cn.imaq.autumn.rpc.exception.AutumnInvokeException;
import cn.imaq.autumn.rpc.serialization.AutumnSerialization;

import java.lang.reflect.InvocationTargetException;

public interface AutumnInvoker {
    Object invoke(Object instance, AutumnMethod method, Object[] params, AutumnSerialization serialization) throws AutumnInvokeException, InvocationTargetException;
}