package cn.imaq.autumn.rpc.cluster;

import cn.imaq.autumn.rpc.client.AutumnRPCClient;
import cn.imaq.autumn.rpc.client.config.RpcClientConfig;
import cn.imaq.autumn.rpc.cluster.config.RpcClusterClientConfig;
import cn.imaq.autumn.rpc.cluster.loadbalance.LoadBalancer;
import cn.imaq.autumn.rpc.config.RpcConfigBase;
import cn.imaq.autumn.rpc.registry.ServiceProviderEntry;
import cn.imaq.autumn.rpc.registry.exception.RpcRegistryException;
import cn.imaq.autumn.rpc.server.exception.RpcInvocationException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class AutumnRPCClusterClient {
    private RpcClusterClientConfig config;
    private Map<ServiceProviderEntry, AutumnRPCClient> rpcClients = new ConcurrentHashMap<>();

    public AutumnRPCClusterClient(RpcClusterClientConfig config) {
        this.config = config;
        try {
            config.getRegistry().start();
        } catch (RpcRegistryException e) {
            log.error("Failed to start registry: {}", String.valueOf(e));
        }
    }

    public Object invoke(Class<?> serviceClass, Method method, Object[] args, int timeoutMs, LoadBalancer loadBalancer) throws Throwable {
        String serviceName = serviceClass.getName();
        List<ServiceProviderEntry> providers = config.getRegistry().lookup(serviceName);
        if (providers.size() == 0) {
            throw new RpcInvocationException("No providers found for service " + serviceName);
        }
        ServiceProviderEntry provider = loadBalancer.select(providers, serviceName, method);
        AutumnRPCClient rpcClient = rpcClients.computeIfAbsent(provider, p -> {
            RpcClientConfig clientConfig = RpcClientConfig.builder()
                    .timeoutMs(config.getDefaultTimeoutMs())
                    .httpClient(config.getHttpClient())
                    .proxy(config.getProxy())
                    .build();
            RpcConfigBase.applyConfigStr(provider.getConfigStr(), clientConfig);
            return new AutumnRPCClient(provider.getHost(), provider.getPort(), clientConfig, false);
        });

        return rpcClient.invoke(serviceClass, method, args, timeoutMs);
    }

    public Object invoke(Class<?> serviceClass, Method method, Object[] args) throws Throwable {
        return invoke(serviceClass, method, args, config.getDefaultTimeoutMs(), config.getDefaultLoadBalancer());
    }

    public <T> T getProxy(Class<T> serviceClass, int timeoutMs, LoadBalancer loadBalancer) {
        try {
            config.getRegistry().subscribe(serviceClass.getName());
        } catch (RpcRegistryException e) {
            log.error("Failed to subscribe {}: {}", serviceClass.getName(), String.valueOf(e));
        }

        return config.getProxy().create(serviceClass, (proxy, method, args) -> invoke(serviceClass, method, args, timeoutMs, loadBalancer));
    }

    public <T> T getProxy(Class<T> serviceClass) {
        return getProxy(serviceClass, config.getDefaultTimeoutMs(), config.getDefaultLoadBalancer());
    }
}
