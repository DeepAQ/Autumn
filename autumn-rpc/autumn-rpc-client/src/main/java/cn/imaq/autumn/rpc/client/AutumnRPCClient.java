package cn.imaq.autumn.rpc.client;

import cn.imaq.autumn.rpc.client.config.RpcClientConfig;
import cn.imaq.autumn.rpc.client.net.RpcHttpClient;
import cn.imaq.autumn.rpc.client.proxy.RpcProxy;
import cn.imaq.autumn.rpc.net.RpcRequest;
import cn.imaq.autumn.rpc.net.RpcResponse;
import cn.imaq.autumn.rpc.serialization.RpcSerialization;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;

import static cn.imaq.autumn.rpc.net.RpcResponse.STATUS_OK;

@Slf4j
public class AutumnRPCClient {
    private String host;
    private int port;
    private RpcClientConfig config;

    private RpcHttpClient httpClient;
    private RpcProxy proxy;
    private RpcSerialization serialization;

    public AutumnRPCClient(String host, int port, RpcClientConfig config, boolean useAutoConfig) {
        this.host = host;
        this.port = port;
        this.config = config;
        // auto config negotiation
        if (useAutoConfig) {
            log.info("Fetching config from server ...");
            // TODO new config auto negotiation protocol
        }
        // init fields
        this.httpClient = config.getHttpClient();
        log.info("Using HTTP client: {}", httpClient.getClass().getSimpleName());
        this.proxy = config.getProxy();
        log.info("Using proxy: {}", proxy.getClass().getSimpleName());
        this.serialization = config.getSerialization();
        log.info("Using serialization: {}", serialization.getClass().getSimpleName());
    }

    public AutumnRPCClient(String host, int port) {
        this(host, port, RpcClientConfig.builder().build(), true);
    }

    public AutumnRPCClient(String host, int port, RpcClientConfig config) {
        this(host, port, config, false);
    }

    public <T> T getService(Class<T> interfaze) {
        return getService(interfaze, 3000);
    }

    public <T> T getService(Class<T> interfaze, int timeout) {
        return proxy.create(interfaze, (proxy, method, args) -> {
            String url = "http://" + host + ":" + port + "/" + interfaze.getName();
            RpcRequest request = RpcRequest.builder()
                    .methodName(method.getName())
                    .paramTypes(method.getParameterTypes())
                    .params(args)
                    .build();
            byte[] payload = serialization.serializeRequest(request);
            byte[] response = httpClient.post(url, payload, serialization.contentType(), timeout);
            Class<?> returnType = method.getReturnType();
            RpcResponse rpcResponse = serialization.deserializeResponse(response, returnType);
            if (rpcResponse.getStatus() == STATUS_OK) {
                if (returnType == void.class || returnType == Void.class) {
                    return null;
                }
                return serialization.convertTypes(new Object[]{rpcResponse.getResult()}, new Type[]{method.getGenericReturnType()})[0];
            } else {
                throw (Throwable) rpcResponse.getResult();
            }
        });
    }
}
