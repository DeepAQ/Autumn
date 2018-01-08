package cn.imaq.tompuss.util;

public class TPPathUtil {
    public static String transform(String original) {
        String path = original;
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return path;
    }
}
