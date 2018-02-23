package cn.imaq.autumn.aop.callback;

import cn.imaq.autumn.aop.HookChain;
import cn.imaq.autumn.aop.HookModel;
import cn.imaq.autumn.core.context.AutumnContext;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

public class AopMethodInterceptor implements MethodInterceptor {
    private List<HookModel> classHooks;

    private AutumnContext context;

    private Object target;

    public AopMethodInterceptor(List<HookModel> classHooks, AutumnContext context, Object target) {
        this.classHooks = classHooks;
        this.context = context;
        this.target = target;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        List<HookModel> methodHooks = classHooks.stream().filter(hm -> hm.matches(method)).collect(Collectors.toList());
        return new HookChain(methodHooks.iterator(), context, target, method, args).proceed();
    }
}
