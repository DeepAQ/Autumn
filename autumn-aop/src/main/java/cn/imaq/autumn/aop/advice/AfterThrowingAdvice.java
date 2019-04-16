package cn.imaq.autumn.aop.advice;

import cn.imaq.autumn.aop.invocation.AopNonProceedingMethodInvocation;
import cn.imaq.autumn.core.context.AutumnContext;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

public class AfterThrowingAdvice extends Advice {
    private int throwingArgIndex;

    public AfterThrowingAdvice(AutumnContext autumnContext, String expression, Method adviceMethod, int throwingArgIndex) {
        super(autumnContext, expression, adviceMethod);
        this.throwingArgIndex = throwingArgIndex;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        try {
            return invocation.proceed();
        } catch (Throwable t) {
            invokeAdvice(new AopNonProceedingMethodInvocation(invocation), t, throwingArgIndex);
            throw t;
        }
    }
}
