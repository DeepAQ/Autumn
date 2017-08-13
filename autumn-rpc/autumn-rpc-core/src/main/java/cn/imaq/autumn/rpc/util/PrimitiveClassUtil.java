package cn.imaq.autumn.rpc.util;

import java.util.HashMap;
import java.util.Map;

public class PrimitiveClassUtil {
    private static Map<String, Class> primClasses = new HashMap<>();

    static {
        primClasses.put("void", void.class);
        primClasses.put("boolean", boolean.class);
        primClasses.put("java.lang.Boolean", Boolean.class);
        primClasses.put("byte", byte.class);
        primClasses.put("java.lang.Byte", Byte.class);
        primClasses.put("char", char.class);
        primClasses.put("java.lang.Character", Character.class);
        primClasses.put("short", short.class);
        primClasses.put("java.lang.Short", Short.class);
        primClasses.put("int", int.class);
        primClasses.put("java.lang.Integer", Integer.class);
        primClasses.put("long", long.class);
        primClasses.put("java.lang.Long", Long.class);
        primClasses.put("float", float.class);
        primClasses.put("java.lang.Float", Float.class);
        primClasses.put("double", double.class);
        primClasses.put("java.lang.Double", Double.class);
        primClasses.put("java.lang.String", String.class);
    }

    public static Class get(String name) {
        return primClasses.get(name);
    }
}
