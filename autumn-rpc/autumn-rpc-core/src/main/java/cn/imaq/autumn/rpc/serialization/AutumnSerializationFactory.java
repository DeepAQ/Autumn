package cn.imaq.autumn.rpc.serialization;

public class AutumnSerializationFactory {
    private static AutumnSerialization defaultSerialization() {
        return new JsonSerialization();
    }

    public static AutumnSerialization getSerialization(String type) {
        if (type == null) {
            return defaultSerialization();
        }
        switch (type) {
            case "jackson":
                return new JsonSerialization();
            default:
                return defaultSerialization();
        }
    }
}
