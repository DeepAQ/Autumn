package cn.imaq.autumn.rpc.client.proxy;

public class AutumnProxyFactory {
    private static AutumnProxy defaultProxy() {
        return new JavaProxy();
    }

    public static AutumnProxy getProxy(String type) {
        if (type == null) {
            return defaultProxy();
        }
        switch (type) {
            case "java":
                return new JavaProxy();
            default:
                return defaultProxy();
        }
    }
}
