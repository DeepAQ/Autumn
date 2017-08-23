package cn.imaq.autumn.rpc.client.net;

public class RPCHttpClientFactory {
    private static RPCHttpClient defaultHttpClient() {
        return new AutumnHttpClient();
    }

    public static RPCHttpClient getHttpClient(String type) {
        if (type == null) {
            return defaultHttpClient();
        }
        switch (type) {
            case "autumn":
                return new AutumnHttpClient();
            case "java":
                return new BasicHttpClient();
            default:
                return defaultHttpClient();
        }
    }
}
