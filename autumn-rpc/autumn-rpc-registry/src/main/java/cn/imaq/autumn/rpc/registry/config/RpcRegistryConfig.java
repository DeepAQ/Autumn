package cn.imaq.autumn.rpc.registry.config;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public abstract class RpcRegistryConfig {
    @Builder.Default
    private int keepAliveTimeout = 10;

    @Builder.Default
    private int keepAliveInterval = 5;
}
