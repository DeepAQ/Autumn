package cn.imaq.autumn.rpc.cluster.loadbalance;

import cn.imaq.autumn.rpc.registry.ServiceProviderEntry;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomLoadBalancer implements LoadBalancer {
    @Override
    public ServiceProviderEntry select(List<ServiceProviderEntry> providers, String serviceName, Method method) {
        return providers.get(ThreadLocalRandom.current().nextInt(providers.size()));
    }
}
