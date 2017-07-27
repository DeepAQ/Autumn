package cn.imaq.autumn.rpc.server.scanner;

import cn.imaq.autumn.rpc.server.annotation.AutumnRPCExpose;
import cn.imaq.autumn.rpc.server.net.InstanceMap;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BasicScanner implements AutumnRPCScanner {
    @Override
    public void process(FastClasspathScanner classpathScanner, InstanceMap instanceMap) {
        classpathScanner.matchClassesWithAnnotation(AutumnRPCExpose.class, clz -> {
            log.info("Exposing: " + clz.getName());
            try {
                Object instance = clz.newInstance();
                instanceMap.putInstance(instance);
                for (Class intf : clz.getInterfaces()) {
                    instanceMap.putInstance(intf.getName(), instance);
                }
            } catch (InstantiationException e) {
                log.warn("Error instantiating " + clz.getName() + ": no nullary constructor found");
            } catch (IllegalAccessException e) {
                log.warn("Error instantiating " + clz.getName() + ": illegal access");
            }
        });
    }
}
