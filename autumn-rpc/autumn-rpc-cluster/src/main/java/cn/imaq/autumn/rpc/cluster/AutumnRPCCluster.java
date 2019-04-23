package cn.imaq.autumn.rpc.cluster;

import cn.imaq.autumn.core.context.AutumnContext;
import cn.imaq.autumn.rpc.cluster.config.RpcClusterServerConfig;
import cn.imaq.autumn.rpc.registry.exception.RpcRegistryException;
import cn.imaq.autumn.rpc.util.AutumnRPCBanner;

import java.io.IOException;

public class AutumnRPCCluster {
    private static AutumnRPCClusterServer clusterServer;

    public static void start(RpcClusterServerConfig config) throws IOException, RpcRegistryException {
        AutumnRPCBanner.printBanner();

        // Init default context
        AutumnContext appContext = new AutumnContext("AutumnRPCContext");
        appContext.scanComponents();

        clusterServer = new AutumnRPCClusterServer(appContext, config);
        clusterServer.start();
    }

    public static void stop() throws IOException {
        if (clusterServer != null) {
            clusterServer.stop();
        }
    }
}
