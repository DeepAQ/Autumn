package cn.imaq.autumn.core.beans.scanner;

import cn.imaq.autumn.core.annotation.Component;
import cn.imaq.autumn.core.beans.BeanInfo;
import cn.imaq.autumn.core.beans.creator.NormalBeanCreator;
import cn.imaq.autumn.core.context.AutumnContext;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanSpec;

public class ComponentBeanScanner implements BeanScanner {

    @Override
    public void process(ScanSpec spec, AutumnContext context) {
        spec.matchClassesWithAnnotation(Component.class, cls -> {
            Component anno = cls.getAnnotation(Component.class);
            String name = anno.value();
            if (name.isEmpty()) {
                name = cls.getSimpleName().toLowerCase();
            }
            context.addBeanInfo(BeanInfo.builder()
                    .name(name)
                    .type(cls)
                    .singleton(anno.singleton())
                    .creator(new NormalBeanCreator(cls))
                    .build());
        });
    }
}
