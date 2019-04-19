package cn.imaq.autumn.rpc.server.context;

import cn.imaq.autumn.core.context.AutumnContext;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RpcContext {
    public static final String ATTR = RpcContext.class.getName();

    private final Map<String, Class<?>> services = new HashMap<>();

    public static RpcContext getFrom(AutumnContext context) {
        RpcContext rpcContext = (RpcContext) context.getAttribute(ATTR);
        if (rpcContext == null) {
            synchronized (context) {
                rpcContext = (RpcContext) context.getAttribute(ATTR);
                if (rpcContext == null) {
                    rpcContext = new RpcContext();
                    context.setAttribute(ATTR, rpcContext);
                }
            }
        }
        return rpcContext;
    }

    public Class<?> findServiceClass(String serviceName) {
        return services.get(serviceName);
    }

    public void registerService(String serviceName, Class<?> serviceClass) {
        log.info("Registering service {{} => {}}", serviceName, serviceClass.getName());
        if (services.put(serviceName, serviceClass) != null) {
            log.warn("{} has multiple implementations, replacing with {}", serviceName, serviceClass.getName());
        }
    }

    public void registerService(Class<?> serviceClass) {
        registerService(serviceClass.getName(), serviceClass);
    }
}