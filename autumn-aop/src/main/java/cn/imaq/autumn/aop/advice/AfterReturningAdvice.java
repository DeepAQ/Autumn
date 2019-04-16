package cn.imaq.autumn.aop.advice;

import cn.imaq.autumn.aop.invocation.AopNonProceedingMethodInvocation;
import cn.imaq.autumn.core.context.AutumnContext;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

public class AfterReturningAdvice extends Advice {
    private int returningArgIndex;

    public AfterReturningAdvice(AutumnContext autumnContext, String expression, Method adviceMethod, int returningArgIndex) {
        super(autumnContext, expression, adviceMethod);
        this.returningArgIndex = returningArgIndex;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object ret = invocation.proceed();
        invokeAdvice(new AopNonProceedingMethodInvocation(invocation), ret, returningArgIndex);
        return ret;
    }
}
