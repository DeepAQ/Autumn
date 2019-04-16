package cn.imaq.autumn.aop.advice;

import cn.imaq.autumn.aop.invocation.AopNonProceedingMethodInvocation;
import cn.imaq.autumn.core.context.AutumnContext;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

public class AfterAdvice extends Advice {
    public AfterAdvice(AutumnContext autumnContext, String expression, Method adviceMethod) {
        super(autumnContext, expression, adviceMethod);
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        try {
            return invocation.proceed();
        } finally {
            invokeAdvice(new AopNonProceedingMethodInvocation(invocation));
        }
    }
}
