package cn.imaq.autumn.rpc.client.config;

import cn.imaq.autumn.rpc.client.net.AutumnHttpClient;
import cn.imaq.autumn.rpc.client.net.RpcHttpClient;
import cn.imaq.autumn.rpc.client.proxy.JavaProxy;
import cn.imaq.autumn.rpc.client.proxy.RpcProxy;
import cn.imaq.autumn.rpc.config.RpcConfigBase;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class RpcClientConfig extends RpcConfigBase {
    @Builder.Default
    private RpcHttpClient httpClient = new AutumnHttpClient();

    @Builder.Default
    private RpcProxy proxy = new JavaProxy();
}
