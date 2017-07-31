package cn.imaq.autumn.rpc.server.net;

public class AutumnHttpServerFactory {
    private static AbstractAutumnHttpServer defaultServer(String host, int port, AutumnHttpHandler handler) {
        return new RapidoidHttpServer(host, port, handler);
    }

    public static AbstractAutumnHttpServer create(String type, String host, int port, AutumnHttpHandler handler) {
        if (type == null) {
            return defaultServer(host, port, handler);
        }
        switch (type) {
            case "rapidoid":
                return new RapidoidHttpServer(host, port, handler);
            default:
                return defaultServer(host, port, handler);
        }
    }
}
