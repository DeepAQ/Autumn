package cn.imaq.autumn.rpc.serialization;

public class AutumnSerializationFactory {
    private static RpcSerialization defaultSerialization() {
        return new JsonSerialization();
    }

    public static RpcSerialization getSerialization(String type) {
        if (type == null) {
            return defaultSerialization();
        }
        switch (type) {
            case "jackson":
                return new JsonSerialization();
            case "hessian":
                return new HessianSerialization();
            default:
                return defaultSerialization();
        }
    }
}
