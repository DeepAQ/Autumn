package cn.imaq.autumn.core.beans.scanner;

import cn.imaq.autumn.core.annotation.BeanFactory;
import cn.imaq.autumn.core.beans.BeanInfo;
import cn.imaq.autumn.core.beans.creator.BeanFactoryCreator;
import cn.imaq.autumn.core.context.AutumnContext;
import cn.imaq.autumn.cpscan.ScanResult;

public class BeanFactoryScanner implements BeanScanner {
    @Override
    public void process(ScanResult result, AutumnContext context) {
        result.getMethodsWithAnnotation(BeanFactory.class).forEach(method -> {
            if (method.getParameterCount() == 0) {
                BeanFactory anno = method.getAnnotation(BeanFactory.class);
                String name = anno.value();
                if (name.isEmpty()) {
                    name = method.getName().toLowerCase();
                }
                context.addBeanInfo(BeanInfo.builder()
                        .name(name)
                        .type(method.getReturnType())
                        .singleton(anno.singleton())
                        .creator(new BeanFactoryCreator(context, method))
                        .build());
            }
        });
    }
}
