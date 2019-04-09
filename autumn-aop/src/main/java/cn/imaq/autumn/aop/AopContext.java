package cn.imaq.autumn.aop;

import cn.imaq.autumn.aop.advice.Advice;
import cn.imaq.autumn.core.context.AutumnContext;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class AopContext {
    public static final String ATTR = AopContext.class.getName();

    private Queue<Advice> advice = new ConcurrentLinkedQueue<>();

    private AopContext() {
    }

    public static AopContext getFrom(AutumnContext context) {
        AopContext aopContext = (AopContext) context.getAttribute(ATTR);
        if (aopContext == null) {
            synchronized (context) {
                aopContext = (AopContext) context.getAttribute(ATTR);
                if (aopContext == null) {
                    aopContext = new AopContext();
                    context.setAttribute(ATTR, aopContext);
                }
            }
        }
        return aopContext;
    }

    public void addAdvice(Advice model) {
        advice.add(model);
    }

    public List<Advice> getAdviceForClass(Class<?> clazz) {
        return advice.stream().filter(a -> a.matches(clazz)).collect(Collectors.toList());
    }
}
