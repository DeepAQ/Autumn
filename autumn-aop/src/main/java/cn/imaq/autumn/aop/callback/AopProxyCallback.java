package cn.imaq.autumn.aop.callback;

import cn.imaq.autumn.aop.invocation.AopMethodInvocation;
import cn.imaq.autumn.aop.advice.Advice;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

public class AopProxyCallback implements MethodInterceptor {
    private List<Advice> classAdvice;

    private Object target;

    public AopProxyCallback(List<Advice> classAdvice, Object target) {
        this.classAdvice = classAdvice;
        this.target = target;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        Iterator<Advice> methodAdviceItr = classAdvice.stream().filter(hm -> hm.matches(method)).iterator();
        return new AopMethodInvocation(methodAdviceItr, target, method, args).proceed();
    }
}
