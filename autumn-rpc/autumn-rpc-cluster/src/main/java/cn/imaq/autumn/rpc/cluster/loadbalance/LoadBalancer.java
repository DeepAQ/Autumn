package cn.imaq.autumn.rpc.cluster.loadbalance;

import cn.imaq.autumn.rpc.registry.ServiceProviderEntry;

import java.lang.reflect.Method;
import java.util.List;

public interface LoadBalancer {
    ServiceProviderEntry select(List<ServiceProviderEntry> providers, String serviceName, Method method);
}
