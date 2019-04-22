package cn.imaq.autumn.rpc.registry.etcd3;

import cn.imaq.autumn.rpc.registry.config.RpcRegistryConfig;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class Etcd3RegistryConfig extends RpcRegistryConfig {
    private String[] endpoints;

    @Builder.Default
    private String keyPrefix = "/AutumnCluster";
}
