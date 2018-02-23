package cn.imaq.autumn.aop;

import cn.imaq.autumn.core.context.AutumnContext;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class AopContext {
    public static final String ATTR = AopContext.class.getName();

    private Queue<HookModel> hooks = new ConcurrentLinkedQueue<>();

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

    public void addHook(HookModel model) {
        hooks.add(model);
    }

    public List<HookModel> getHooksForClass(Class<?> clazz) {
        return hooks.stream().filter(hm -> hm.matches(clazz)).collect(Collectors.toList());
    }
}
