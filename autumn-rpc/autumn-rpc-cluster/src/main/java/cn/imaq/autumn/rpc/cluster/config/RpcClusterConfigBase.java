package cn.imaq.autumn.rpc.cluster.config;

import cn.imaq.autumn.rpc.registry.ServiceRegistry;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public abstract class RpcClusterConfigBase {
    private ServiceRegistry registry;
}
