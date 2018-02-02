package cn.imaq.autumn.rest.util;

public class PathUtil {
    public static String transform(String original) {
        String path = original;
        while (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return path;
    }
}
