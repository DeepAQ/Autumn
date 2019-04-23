package cn.imaq.autumn.rpc.client;

import cn.imaq.autumn.rpc.client.config.RpcClientConfig;
import cn.imaq.autumn.rpc.client.net.RpcHttpClient;
import cn.imaq.autumn.rpc.client.proxy.RpcProxy;
import cn.imaq.autumn.rpc.config.RpcConfigBase;
import cn.imaq.autumn.rpc.net.RpcRequest;
import cn.imaq.autumn.rpc.net.RpcResponse;
import cn.imaq.autumn.rpc.serialization.RpcSerialization;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
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

        this.httpClient = config.getHttpClient();
        log.info("Using HTTP client: {}", httpClient.getClass().getSimpleName());

        // auto config negotiation
        if (useAutoConfig) {
            log.info("Fetching config from server ...");
            try {
                String configStr = new String(httpClient.get("http://" + host + ":" + port, config.getTimeoutMs()));
                RpcConfigBase.applyConfigStr(configStr, config);
            } catch (Exception e) {
                log.error("Auto config error: {}", String.valueOf(e));
            }
        }

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

    public Object invoke(Class<?> serviceClass, Method method, Object[] args) throws Throwable {
        return invoke(serviceClass, method, args, config.getTimeoutMs());
    }

    public Object invoke(Class<?> serviceClass, Method method, Object[] args, int timeoutMs) throws Throwable {
        String url = "http://" + host + ":" + port + "/" + serviceClass.getName();
        RpcRequest request = RpcRequest.builder()
                .methodName(method.getName())
                .paramTypes(method.getParameterTypes())
                .params(args)
                .build();
        byte[] payload = serialization.serializeRequest(request);
        byte[] response = httpClient.post(url, payload, serialization.contentType(), timeoutMs);
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
    }

    public <T> T getService(Class<T> serviceClass) {
        return getService(serviceClass, config.getTimeoutMs());
    }

    public <T> T getService(Class<T> serviceClass, int timeoutMs) {
        return proxy.create(serviceClass, (proxy, method, args) -> invoke(serviceClass, method, args, timeoutMs));
    }
}
