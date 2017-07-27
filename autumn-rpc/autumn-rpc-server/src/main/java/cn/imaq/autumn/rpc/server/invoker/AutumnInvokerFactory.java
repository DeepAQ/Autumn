package cn.imaq.autumn.rpc.server.invoker;

public class AutumnInvokerFactory {
    private static AutumnInvoker defaultInvoker() {
        return new ReflectAsmInvoker();
    }

    public static AutumnInvoker getInvoker(String type) {
        if (type == null) {
            return defaultInvoker();
        }
        switch (type) {
            case "reflection":
                return new ReflectionInvoker();
            case "reflectasm":
                return new ReflectAsmInvoker();
            default:
                return defaultInvoker();
        }
    }
}
