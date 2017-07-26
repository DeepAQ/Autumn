package cn.imaq.autumn.rpc.client;

import cn.imaq.autumn.rpc.client.net.AutumnHttpClient;
import cn.imaq.autumn.rpc.client.net.BasicHttpClient;
import cn.imaq.autumn.rpc.client.proxy.AutumnProxy;
import cn.imaq.autumn.rpc.client.proxy.JavaProxy;
import cn.imaq.autumn.rpc.net.AutumnRPCRequest;
import cn.imaq.autumn.rpc.net.AutumnRPCResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AutumnRPCClient {
    private String host;
    private int port;

    private AutumnHttpClient httpClient;
    private AutumnProxy proxy;

    public AutumnRPCClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.httpClient = new BasicHttpClient(); // TODO config
        this.proxy = new JavaProxy(); // TODO config
    }

    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> interfaze, int timeout) {
        return proxy.create(interfaze, (proxy, method, args) -> {
            String url = "http://" + host + ":" + port + "/" + interfaze.getName();
            ObjectMapper mapper = new ObjectMapper();
            AutumnRPCRequest request = new AutumnRPCRequest(
                    method.getName(), method.getParameterTypes(), args, mapper
            );
            byte[] payload = mapper.writeValueAsBytes(request);
            byte[] response = httpClient.post(url, payload, "application/json", timeout);
            AutumnRPCResponse rpcResponse = mapper.readValue(response, AutumnRPCResponse.class);
            if (rpcResponse.getStatus() >= 0) {
                return mapper.treeToValue(rpcResponse.getResult(), method.getReturnType());
            } else {
                return mapper.treeToValue(rpcResponse.getResult(), Class.forName(rpcResponse.getResultType()));
            }
        });
    }
}
