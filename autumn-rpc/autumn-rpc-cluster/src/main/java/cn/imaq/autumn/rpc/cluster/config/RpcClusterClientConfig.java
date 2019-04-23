package cn.imaq.autumn.rpc.cluster.config;

import cn.imaq.autumn.rpc.client.net.AutumnHttpClient;
import cn.imaq.autumn.rpc.client.net.RpcHttpClient;
import cn.imaq.autumn.rpc.client.proxy.JavaProxy;
import cn.imaq.autumn.rpc.client.proxy.RpcProxy;
import cn.imaq.autumn.rpc.cluster.loadbalance.LoadBalancer;
import cn.imaq.autumn.rpc.cluster.loadbalance.RandomLoadBalancer;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class RpcClusterClientConfig extends RpcClusterConfigBase {
    @Builder.Default
    private LoadBalancer defaultLoadBalancer = new RandomLoadBalancer();

    @Builder.Default
    private int defaultTimeoutMs = 3000;

    @Builder.Default
    private RpcHttpClient httpClient = new AutumnHttpClient();

    @Builder.Default
    private RpcProxy proxy = new JavaProxy();
}
