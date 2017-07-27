package cn.imaq.autumn.rpc.server.net;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class InstanceMap {
    private final Map<String, Object> map = new ConcurrentHashMap<>();

    public Object getInstance(String serviceName) {
        return map.get(serviceName);
    }

    public void putInstance(String serviceName, Object instance) {
        log.info("Adding service {" + serviceName + " => " + instance.getClass().getName() + "}");
        if (map.put(serviceName, instance) != null) {
            log.warn(serviceName + " has multiple implements, replacing with " + instance.getClass().getName());
        }
    }

    public void putInstance(Object instance) {
        putInstance(instance.getClass().getName(), instance);
    }

    public void removeInstance(String className) {
        map.remove(className);
    }

    public void clear() {
        map.clear();
    }
}