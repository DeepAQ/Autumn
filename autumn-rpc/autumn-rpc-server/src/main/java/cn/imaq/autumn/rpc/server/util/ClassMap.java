package cn.imaq.autumn.rpc.server.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClassMap {
    private Map<String, Class> map = new ConcurrentHashMap<>();

    public Class getClass(String className) {
        return map.get(className);
    }

    public void putClass(String className, Class clz) {
        LogUtil.D("Adding service {" + className + " => " + clz.getName() + "}");
        if (map.put(className, clz) != null) {
            LogUtil.E(className + " has multiple implements, replacing with " + clz.getName());
        }
    }

    public void putClass(Class clz) {
        putClass(clz.getName(), clz);
    }

    public void removeClass(String className) {
        map.remove(className);
    }

    public void removeClass(Class clz) {
        removeClass(clz.getName());
    }

    public void clear() {
        map.clear();
    }
}