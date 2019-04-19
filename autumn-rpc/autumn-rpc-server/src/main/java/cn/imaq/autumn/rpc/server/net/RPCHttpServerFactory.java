package cn.imaq.autumn.rpc.server.net;

import cn.imaq.autumn.rpc.server.handler.RpcRequestHandler;

public class RPCHttpServerFactory {
    private static AbstractRpcHttpServer defaultServer(String host, int port, RpcRequestHandler handler) {
        return new AutumnHttpServer(host, port, handler);
    }

    public static AbstractRpcHttpServer create(String type, String host, int port, RpcRequestHandler handler) {
        if (type == null) {
            return defaultServer(host, port, handler);
        }
        switch (type) {
            case "autumn":
                return new AutumnHttpServer(host, port, handler);
            case "sun":
                return new SunHttpServer(host, port, handler);
            default:
                return defaultServer(host, port, handler);
        }
    }
}
