package cn.imaq.autumn.rpc.server.net;

public class RPCHttpServerFactory {
    private static AbstractRPCHttpServer defaultServer(String host, int port, RPCHttpHandler handler) {
        return new AutumnHttpServer(host, port, handler);
    }

    public static AbstractRPCHttpServer create(String type, String host, int port, RPCHttpHandler handler) {
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
