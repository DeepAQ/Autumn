package cn.imaq.autumn.aop.scanner;

import cn.imaq.autumn.aop.AopContext;
import cn.imaq.autumn.aop.advice.*;
import cn.imaq.autumn.aop.annotation.Aspect;
import cn.imaq.autumn.core.beans.BeanInfo;
import cn.imaq.autumn.core.beans.creator.NormalBeanCreator;
import cn.imaq.autumn.core.beans.scanner.BeanScanner;
import cn.imaq.autumn.core.context.AutumnContext;
import cn.imaq.autumn.cpscan.ScanResult;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AspectBeanScanner implements BeanScanner {
    private static final Map<Class<? extends Annotation>, AdviceAnnotationParser<?>> annotationParsers = new HashMap<>();

    static {
        annotationParsers.put(Before.class, (AdviceAnnotationParser<Before>) (annotation, context, adviceMethod) -> new BeforeAdvice(context, annotation.value(), adviceMethod));
        annotationParsers.put(After.class, (AdviceAnnotationParser<After>) (annotation, context, adviceMethod) -> new AfterAdvice(context, annotation.value(), adviceMethod));
        annotationParsers.put(Around.class, (AdviceAnnotationParser<Around>) (annotation, context, adviceMethod) -> new AroundAdvice(context, annotation.value(), adviceMethod));
        annotationParsers.put(AfterReturning.class, (AdviceAnnotationParser<AfterReturning>) (annotation, context, adviceMethod) -> {
            String expr = annotation.pointcut();
            if (expr.isEmpty()) {
                expr = annotation.value();
            }
            return new AfterReturningAdvice(context, expr, adviceMethod, getArgIndex(annotation.returning(), annotation.argNames(), adviceMethod));
        });
        annotationParsers.put(AfterThrowing.class, (AdviceAnnotationParser<AfterThrowing>) (annotation, context, adviceMethod) -> {
            String expr = annotation.pointcut();
            if (expr.isEmpty()) {
                expr = annotation.value();
            }
            return new AfterThrowingAdvice(context, expr, adviceMethod, getArgIndex(annotation.throwing(), annotation.argNames(), adviceMethod));
        });
    }

    private static int getArgIndex(String name, String argNames, Method method) {
        Parameter[] params = method.getParameters();
        String[] names = argNames.split(",");
        for (int i = 0; i < params.length; i++) {
            if ((names.length > i && names[i].trim().equals(name)) || (params[i].isNamePresent() && params[i].getName().equals(name))) {
                return i;
            }
        }
        return -1;
    }

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
                for (Class<? extends Annotation> annotationClass : annotationParsers.keySet()) {
                    if (method.isAnnotationPresent(annotationClass)) {
                        method.setAccessible(true);
                        Object annotation = method.getAnnotation(annotationClass);
                        log.info("Adding advice {} {}", annotation, method);
                        aopContext.addAdvice(annotationParsers.get(annotationClass).parse(annotation, context, method));
                        break;
                    }
                }
            }
        });
    }

    @FunctionalInterface
    private interface AdviceAnnotationParser<T extends Annotation> {
        @SuppressWarnings("unchecked")
        default Advice parse(Object annotation, AutumnContext context, Method adviceMethod) {
            return parse((T) annotation, context, adviceMethod);
        }

        Advice parse(T annotation, AutumnContext context, Method adviceMethod);
    }
}
