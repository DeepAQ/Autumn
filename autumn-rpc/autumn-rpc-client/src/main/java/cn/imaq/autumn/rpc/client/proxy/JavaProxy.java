package cn.imaq.autumn.rpc.client.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class JavaProxy implements AutumnProxy {
    @SuppressWarnings("unchecked")
    @Override
    public <T> T create(Class<T> interfaze, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class[]{interfaze}, handler);
    }
}
