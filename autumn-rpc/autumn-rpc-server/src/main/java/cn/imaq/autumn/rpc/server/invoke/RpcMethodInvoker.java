package cn.imaq.autumn.rpc.server.invoke;

import cn.imaq.autumn.rpc.serialization.RpcSerialization;
import cn.imaq.autumn.rpc.server.exception.RpcInvocationException;

import java.lang.reflect.InvocationTargetException;

public interface RpcMethodInvoker {
    Object invoke(Object instance, RpcMethod method, Object[] params, RpcSerialization serialization) throws RpcInvocationException, InvocationTargetException;
}