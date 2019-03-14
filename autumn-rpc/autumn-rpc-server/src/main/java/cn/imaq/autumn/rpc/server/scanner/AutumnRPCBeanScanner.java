package cn.imaq.autumn.rpc.server.scanner;

import cn.imaq.autumn.core.beans.BeanInfo;
import cn.imaq.autumn.core.beans.creator.NormalBeanCreator;
import cn.imaq.autumn.core.beans.scanner.BeanScanner;
import cn.imaq.autumn.core.context.AutumnContext;
import cn.imaq.autumn.cpscan.ScanResult;
import cn.imaq.autumn.rpc.server.annotation.AutumnRPCExpose;
import cn.imaq.autumn.rpc.server.net.ServiceMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AutumnRPCBeanScanner implements BeanScanner {
    @Override
    public void process(ScanResult result, AutumnContext context) {
        ServiceMap serviceMap = (ServiceMap) context.getAttribute(ServiceMap.ATTR);
        if (serviceMap != null) {
            result.getClassesWithAnnotation(AutumnRPCExpose.class).forEach(cls -> {
                log.info("RPC Exposing: {}", cls.getName());
                serviceMap.addService(cls);
                for (Class intf : cls.getInterfaces()) {
                    serviceMap.addService(intf.getName(), cls);
                }
                context.addBeanInfo(BeanInfo.builder()
                        .type(cls)
                        .singleton(true)
                        .creator(new NormalBeanCreator(cls))
                        .build());
            });
        }
    }
}
