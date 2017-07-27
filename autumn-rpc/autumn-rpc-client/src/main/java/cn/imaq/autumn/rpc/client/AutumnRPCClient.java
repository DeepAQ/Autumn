package cn.imaq.autumn.rpc.client;

import cn.imaq.autumn.rpc.client.net.AutumnHttpClient;
import cn.imaq.autumn.rpc.client.net.BasicHttpClient;
import cn.imaq.autumn.rpc.client.proxy.AutumnProxy;
import cn.imaq.autumn.rpc.client.proxy.JavaProxy;
import cn.imaq.autumn.rpc.net.AutumnRPCRequest;
import cn.imaq.autumn.rpc.net.AutumnRPCResponse;
import cn.imaq.autumn.rpc.serialization.AutumnSerialization;
import cn.imaq.autumn.rpc.serialization.JsonSerialization;

import static cn.imaq.autumn.rpc.net.AutumnRPCResponse.STATUS_OK;

public class AutumnRPCClient {
    private String host;
    private int port;

    private AutumnHttpClient httpClient;
    private AutumnProxy proxy;
    private AutumnSerialization serialization;

    public AutumnRPCClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.httpClient = new BasicHttpClient(); // TODO config
        this.proxy = new JavaProxy(); // TODO config
        this.serialization = new JsonSerialization(); // TODO config
    }

    public <T> T getService(Class<T> interfaze, int timeout) {
        return proxy.create(interfaze, (proxy, method, args) -> {
            String url = "http://" + host + ":" + port + "/" + interfaze.getName();
            AutumnRPCRequest request = AutumnRPCRequest.builder()
                    .methodName(method.getName())
                    .paramTypes(method.getParameterTypes())
                    .params(args)
                    .build();
            byte[] payload = serialization.serializeRequest(request);
            byte[] response = httpClient.post(url, payload, "application/json", timeout);
            AutumnRPCResponse rpcResponse = serialization.deserializeResponse(response, method.getReturnType());
            if (rpcResponse.getStatus() == STATUS_OK) {
                return rpcResponse.getResult();
            } else {
                throw (Throwable) rpcResponse.getResult();
            }
        });
    }
}
