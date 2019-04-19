package cn.imaq.autumn.rpc.server.config;

import cn.imaq.autumn.rpc.config.RpcConfigBase;
import cn.imaq.autumn.rpc.server.invoke.ReflectAsmInvoker;
import cn.imaq.autumn.rpc.server.invoke.RpcMethodInvoker;
import cn.imaq.autumn.rpc.server.net.AutumnHttpServer;
import cn.imaq.autumn.rpc.server.net.RpcHttpServer;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class RpcServerConfig extends RpcConfigBase {
    @Builder.Default
    private String host = "0.0.0.0";

    @Builder.Default
    private int port = 8801;

    @Builder.Default
    private RpcHttpServer httpServer = new AutumnHttpServer();

    @Builder.Default
    private RpcMethodInvoker methodInvoker = new ReflectAsmInvoker();
}
