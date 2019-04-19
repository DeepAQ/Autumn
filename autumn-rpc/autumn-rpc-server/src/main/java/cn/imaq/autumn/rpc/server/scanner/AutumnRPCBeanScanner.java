package cn.imaq.autumn.rpc.server.scanner;

import cn.imaq.autumn.core.beans.BeanInfo;
import cn.imaq.autumn.core.beans.creator.NormalBeanCreator;
import cn.imaq.autumn.core.beans.scanner.BeanScanner;
import cn.imaq.autumn.core.context.AutumnContext;
import cn.imaq.autumn.cpscan.ScanResult;
import cn.imaq.autumn.rpc.server.annotation.AutumnRPCExpose;
import cn.imaq.autumn.rpc.server.context.RpcContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AutumnRPCBeanScanner implements BeanScanner {
    @Override
    public void process(ScanResult result, AutumnContext context) {
        RpcContext rpcContext = (RpcContext) context.getAttribute(RpcContext.ATTR);
        if (rpcContext != null) {
            result.getClassesWithAnnotation(AutumnRPCExpose.class).forEach(cls -> {
                log.info("RPC Exposing: {}", cls.getName());
                rpcContext.registerService(cls);
                for (Class<?> intf : cls.getInterfaces()) {
                    rpcContext.registerService(intf.getName(), cls);
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
