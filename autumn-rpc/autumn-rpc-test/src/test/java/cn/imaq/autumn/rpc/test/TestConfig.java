package cn.imaq.autumn.rpc.test;

import cn.imaq.autumn.core.annotation.Component;
import cn.imaq.autumn.rpc.cluster.AutumnRPCCluster;
import cn.imaq.autumn.rpc.cluster.AutumnRPCClusterServer;
import cn.imaq.autumn.rpc.cluster.config.RpcClusterServerConfig;
import cn.imaq.autumn.rpc.registry.etcd3.Etcd3RegistryConfig;
import cn.imaq.autumn.rpc.registry.etcd3.Etcd3ServiceRegistry;
import cn.imaq.autumn.rpc.registry.exception.RpcRegistryException;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.io.IOException;

@Component
public class TestConfig {
    private AutumnRPCClusterServer clusterServer;

    @BeforeSuite
    public void startServer() throws IOException, RpcRegistryException, InterruptedException {
        AutumnRPCCluster.start(RpcClusterServerConfig.builder()
                .advertiseHost("127.0.0.1")
                .registry(new Etcd3ServiceRegistry(Etcd3RegistryConfig.builder()
                        .endpoints(new String[]{"http://127.0.0.1:2379"})
                        .build()))
                .build());
    }

    @AfterSuite
    public void stopServer() throws IOException {
        AutumnRPCCluster.stop();
    }
}
