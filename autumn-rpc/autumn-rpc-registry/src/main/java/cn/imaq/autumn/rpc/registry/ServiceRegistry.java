package cn.imaq.autumn.rpc.registry;

import cn.imaq.autumn.rpc.registry.exception.RpcRegistryException;

import java.util.List;

public interface ServiceRegistry {
    void start() throws RpcRegistryException;

    void stop() throws RpcRegistryException;

    void register(ServiceProviderEntry provider) throws RpcRegistryException;

    void deregister(ServiceProviderEntry provider) throws RpcRegistryException;

    void subscribe(String serviceName) throws RpcRegistryException;

    void unsubscribe(String serviceName) throws RpcRegistryException;

    List<ServiceProviderEntry> lookup(String serviceName, boolean forceUpdate) throws RpcRegistryException;

    default List<ServiceProviderEntry> lookup(String serviceName) throws RpcRegistryException {
        return lookup(serviceName, false);
    }
}
