package cn.imaq.autumn.rpc.server.scanner;

import cn.imaq.autumn.core.beans.BeanInfo;
import cn.imaq.autumn.core.beans.creator.NormalBeanCreator;
import cn.imaq.autumn.core.beans.scanner.BeanScanner;
import cn.imaq.autumn.core.context.AutumnContext;
import cn.imaq.autumn.rpc.server.annotation.AutumnRPCExpose;
import cn.imaq.autumn.rpc.server.net.ServiceMap;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanSpec;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AutumnRPCBeanScanner implements BeanScanner {
    @Override
    public void process(ScanSpec spec, AutumnContext context) {
        ServiceMap serviceMap = (ServiceMap) context.getAttribute(ServiceMap.ATTR);
        if (serviceMap != null) {
            spec.matchClassesWithAnnotation(AutumnRPCExpose.class, clz -> {
                log.info("Exposing: " + clz.getName());
                serviceMap.addService(clz);
                for (Class intf : clz.getInterfaces()) {
                    serviceMap.addService(intf.getName(), clz);
                }
                context.addBeanInfo(BeanInfo.builder()
                        .type(clz)
                        .singleton(true)
                        .creator(new NormalBeanCreator(clz))
                        .build());
            });
        }
    }
}
