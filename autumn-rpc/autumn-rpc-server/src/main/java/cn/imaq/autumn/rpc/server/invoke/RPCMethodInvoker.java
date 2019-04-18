package cn.imaq.autumn.rpc.server.invoke;

import cn.imaq.autumn.rpc.exception.RPCInvokeException;
import cn.imaq.autumn.rpc.serialization.RPCSerialization;

import java.lang.reflect.InvocationTargetException;

public interface RPCMethodInvoker {
    Object invoke(Object instance, RPCMethod method, Object[] params, RPCSerialization serialization) throws RPCInvokeException, InvocationTargetException;
}