package cn.imaq.autumn.rpc.registry;

import cn.imaq.autumn.rpc.registry.exception.RpcRegistryException;

import java.util.Collection;

public interface ServiceRegistry {
    void start() throws RpcRegistryException;

    void stop() throws RpcRegistryException;

    void register(ServiceProviderEntry provider) throws RpcRegistryException;

    void deregister(ServiceProviderEntry provider) throws RpcRegistryException;

    void subscribe(String serviceName);

    void unsubscribe(String serviceName);

    Collection<ServiceProviderEntry> lookup(String serviceName, boolean forceUpdate) throws RpcRegistryException;

    default Collection<ServiceProviderEntry> lookup(String serviceName) throws RpcRegistryException {
        return lookup(serviceName, false);
    }
}
