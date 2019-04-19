package cn.imaq.autumn.rpc.server.invoke;

public class AutumnInvokerFactory {
    private static RpcMethodInvoker defaultInvoker() {
        return new ReflectAsmInvoker();
    }

    public static RpcMethodInvoker getInvoker(String type) {
        if (type == null) {
            return defaultInvoker();
        }
        switch (type) {
            case "reflection":
                return new ReflectionInvoker();
            case "reflectasm":
                return new ReflectAsmInvoker();
            case "methodhandle":
                return new MethodHandleInvoker();
            default:
                return defaultInvoker();
        }
    }
}
