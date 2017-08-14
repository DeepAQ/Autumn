package cn.imaq.autumn.rpc.server.invoker;

public class AutumnInvokerFactory {
    private static AutumnInvoker defaultInvoker() {
        return new ReflectionInvoker();
    }

    public static AutumnInvoker getInvoker(String type) {
        if (type == null) {
            return defaultInvoker();
        }
        switch (type) {
            case "reflection":
                return new ReflectionInvoker();
            default:
                return defaultInvoker();
        }
    }
}
