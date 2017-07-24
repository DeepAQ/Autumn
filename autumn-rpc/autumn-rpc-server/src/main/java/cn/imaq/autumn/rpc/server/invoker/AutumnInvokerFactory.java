package cn.imaq.autumn.rpc.server.invoker;

import cn.imaq.autumn.rpc.server.util.ConfigUtil;

public class AutumnInvokerFactory {
    private static AutumnInvoker defaultInvoker() {
        return new ReflectAsmInvoker();
    }

    public static AutumnInvoker getInvoker() {
        String config = ConfigUtil.get("autumn.invoker");
        if (config == null) {
            return defaultInvoker();
        }
        switch (config) {
            case "reflection":
                return new ReflectionInvoker();
            case "reflectasm":
                return new ReflectAsmInvoker();
            default:
                return defaultInvoker();
        }
    }
}
