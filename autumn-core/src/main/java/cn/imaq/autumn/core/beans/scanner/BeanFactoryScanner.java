package cn.imaq.autumn.core.beans.scanner;

import cn.imaq.autumn.core.annotation.BeanFactory;
import cn.imaq.autumn.core.beans.BeanInfo;
import cn.imaq.autumn.core.beans.creator.BeanFactoryCreator;
import cn.imaq.autumn.core.context.AutumnContext;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanSpec;

import java.lang.reflect.Method;

public class BeanFactoryScanner implements BeanScanner {
    @Override
    public void process(ScanSpec spec, AutumnContext context) {
        spec.matchClassesWithMethodAnnotation(BeanFactory.class, (cls, executable) -> {
            if (executable instanceof Method && executable.getParameterCount() == 0) {
                Method method = ((Method) executable);
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
