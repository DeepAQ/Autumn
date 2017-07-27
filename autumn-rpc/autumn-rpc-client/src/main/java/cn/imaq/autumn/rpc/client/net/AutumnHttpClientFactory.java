package cn.imaq.autumn.rpc.client.net;

public class AutumnHttpClientFactory {
    private static AutumnHttpClient defaultHttpClient() {
        return new BasicHttpClient();
    }

    public static AutumnHttpClient getHttpClient(String type) {
        if (type == null) {
            return defaultHttpClient();
        }
        switch (type) {
            case "java":
                return new BasicHttpClient();
            default:
                return defaultHttpClient();
        }
    }
}
