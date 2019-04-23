package cn.imaq.autumn.rpc.cluster;

import cn.imaq.autumn.core.context.AutumnContext;
import cn.imaq.autumn.rpc.cluster.config.RpcClusterServerConfig;
import cn.imaq.autumn.rpc.registry.ServiceProviderEntry;
import cn.imaq.autumn.rpc.registry.exception.RpcRegistryException;
import cn.imaq.autumn.rpc.server.AutumnRPCServer;
import cn.imaq.autumn.rpc.server.context.RpcContext;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class AutumnRPCClusterServer {
    private AutumnContext context;
    private RpcClusterServerConfig clusterServerConfig;
    private AutumnRPCServer rpcServer;

    public AutumnRPCClusterServer(AutumnContext context, RpcClusterServerConfig clusterServerConfig) {
        this.context = context;
        this.clusterServerConfig = clusterServerConfig;
        this.rpcServer = new AutumnRPCServer(context, clusterServerConfig.getServerConfig());
    }

    public synchronized void start() throws RpcRegistryException, IOException {
        // start registry
        try {
            clusterServerConfig.getRegistry().start();
        } catch (RpcRegistryException e) {
            log.error("Failed to start registry: {}", String.valueOf(e));
            throw e;
        }

        // start server
        rpcServer.start();

        // register services to registry
        RpcContext rpcContext = RpcContext.getFrom(context);
        for (String serviceName : rpcContext.getRegisteredServiceNames()) {
            try {
                clusterServerConfig.getRegistry().register(ServiceProviderEntry.builder()
                        .serviceName(serviceName)
                        .host(clusterServerConfig.getAdvertiseHost())
                        .port(clusterServerConfig.getServerConfig().getPort())
                        .configStr(clusterServerConfig.getServerConfig().toConfigStr())
                        .build());
            } catch (RpcRegistryException e) {
                log.error("Failed to register service {}: {}", serviceName, String.valueOf(e));
            }
        }
    }

    public synchronized void stop() throws IOException {
        // deregister services to registry
        RpcContext rpcContext = RpcContext.getFrom(context);
        for (String serviceName : rpcContext.getRegisteredServiceNames()) {
            try {
                clusterServerConfig.getRegistry().deregister(ServiceProviderEntry.builder()
                        .serviceName(serviceName)
                        .host(clusterServerConfig.getAdvertiseHost())
                        .port(clusterServerConfig.getServerConfig().getPort())
                        .configStr(clusterServerConfig.getServerConfig().toConfigStr())
                        .build());
            } catch (RpcRegistryException e) {
                log.error("Failed to deregister service {}: {}", serviceName, String.valueOf(e));
            }
        }

        // stop server
        rpcServer.stop();

        // stop registry
        try {
            clusterServerConfig.getRegistry().stop();
        } catch (RpcRegistryException e) {
            log.error("Failed to stop registry: {}", String.valueOf(e));
        }
    }
}
