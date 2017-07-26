package cn.imaq.autumn.rpc.server.scanner;

import cn.imaq.autumn.rpc.server.annotation.AutumnRPCExpose;
import cn.imaq.autumn.rpc.server.net.InstanceMap;
import cn.imaq.autumn.rpc.util.LogUtil;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;

public class BasicScanner implements AutumnRPCScanner {
    @Override
    public void process(FastClasspathScanner classpathScanner, InstanceMap instanceMap) {
        classpathScanner.matchClassesWithAnnotation(AutumnRPCExpose.class, clz -> {
            LogUtil.I("Exposing: " + clz.getName());
            try {
                Object instance = clz.newInstance();
                instanceMap.putInstance(instance);
                for (Class intf : clz.getInterfaces()) {
                    instanceMap.putInstance(intf.getName(), instance);
                }
            } catch (InstantiationException e) {
                LogUtil.I("Error instantiating " + clz.getName() + ": no nullary constructor found");
            } catch (IllegalAccessException e) {
                LogUtil.I("Error instantiating " + clz.getName() + ": illegal access");
            }
        });
    }
}
