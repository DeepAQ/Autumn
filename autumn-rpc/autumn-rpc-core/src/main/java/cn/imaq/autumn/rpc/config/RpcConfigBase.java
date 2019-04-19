package cn.imaq.autumn.rpc.config;

import cn.imaq.autumn.rpc.serialization.JsonSerialization;
import cn.imaq.autumn.rpc.serialization.RpcSerialization;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public abstract class RpcConfigBase {
    @Builder.Default
    private RpcSerialization serialization = new JsonSerialization();
}
