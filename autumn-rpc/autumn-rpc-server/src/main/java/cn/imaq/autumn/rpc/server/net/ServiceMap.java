package cn.imaq.autumn.rpc.server.net;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ServiceMap {
    public static final String ATTR = ServiceMap.class.getName();

    private final Map<String, Class<?>> map = new HashMap<>();

    public Class<?> getServiceClass(String serviceName) {
        return map.get(serviceName);
    }

    public void addService(String serviceName, Class<?> serviceClass) {
        log.info("Adding service {" + serviceName + " => " + serviceClass.getName() + "}");
        if (map.put(serviceName, serviceClass) != null) {
            log.warn(serviceName + " has multiple implements, replacing with " + serviceClass.getName());
        }
    }

    public void addService(Class<?> serviceClass) {
        addService(serviceClass.getName(), serviceClass);
    }

    public void removeService(String serviceName) {
        map.remove(serviceName);
    }

    public void clear() {
        map.clear();
    }
}