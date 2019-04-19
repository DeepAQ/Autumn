package cn.imaq.autumn.rpc.client.proxy;

import java.lang.reflect.InvocationHandler;

public interface RpcProxy {
    <T> T create(Class<T> interfaze, InvocationHandler handler);
}
