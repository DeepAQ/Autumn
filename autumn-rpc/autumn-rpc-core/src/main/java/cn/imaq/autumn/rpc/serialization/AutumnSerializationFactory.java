package cn.imaq.autumn.rpc.serialization;

public class AutumnSerializationFactory {
    private static RPCSerialization defaultSerialization() {
        return new JsonSerialization();
    }

    public static RPCSerialization getSerialization(String type) {
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
