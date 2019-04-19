package cn.imaq.autumn.rpc.server;

import cn.imaq.autumn.core.beans.BeanInfo;
import cn.imaq.autumn.core.context.AutumnContext;
import cn.imaq.autumn.rpc.server.config.RpcServerConfig;
import cn.imaq.autumn.rpc.util.AutumnRPCBanner;

public class AutumnRPC {
    private static AutumnRPCServer rpcServer;

    public static void start() {
        start(null);
    }

    public static void start(RpcServerConfig config) {
        AutumnRPCBanner.printBanner();

        // Init default context
        AutumnContext appContext = new AutumnContext("AutumnRPCContext");
        if (config != null) {
            appContext.addBeanInfo(BeanInfo.builder()
                    .name("rpcServerConfig")
                    .type(RpcServerConfig.class)
                    .singleton(true)
                    .creator(() -> config)
                    .build());
        }
        appContext.scanComponents();

        rpcServer = new AutumnRPCServer(appContext);
        rpcServer.start();
    }

    public static void stop() {
        if (rpcServer != null) {
            rpcServer.stop();
        }
    }
}
