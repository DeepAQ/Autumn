package cn.imaq.autumn.aop.scanner;

import cn.imaq.autumn.aop.AopContext;
import cn.imaq.autumn.aop.advice.AroundAdvice;
import cn.imaq.autumn.aop.annotation.Aspect;
import cn.imaq.autumn.core.beans.BeanInfo;
import cn.imaq.autumn.core.beans.creator.NormalBeanCreator;
import cn.imaq.autumn.core.beans.scanner.BeanScanner;
import cn.imaq.autumn.core.context.AutumnContext;
import cn.imaq.autumn.cpscan.ScanResult;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Around;

import java.lang.reflect.Method;

@Slf4j
public class AspectBeanScanner implements BeanScanner {
    @Override
    public void process(ScanResult result, AutumnContext context) {
        AopContext aopContext = AopContext.getFrom(context);
        result.getClassesWithAnnotation(Aspect.class).forEach(cls -> {
            Aspect anno = cls.getAnnotation(Aspect.class);
            String name = anno.value();
            if (name.isEmpty()) {
                name = cls.getSimpleName().toLowerCase();
            }
            context.addBeanInfo(BeanInfo.builder()
                    .name(name)
                    .type(cls)
                    .singleton(true)
                    .creator(new NormalBeanCreator(cls))
                    .build());

            for (Method method : cls.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Around.class)) {
                    method.setAccessible(true);
                    Around aroundAnno = method.getAnnotation(Around.class);
                    log.info("Adding around advice {}", aroundAnno);
                    aopContext.addAdvice(new AroundAdvice(context, aroundAnno.value(), method));
                }
            }
        });
    }
}
