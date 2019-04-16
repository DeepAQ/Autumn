package cn.imaq.autumn.aop.advice;

import cn.imaq.autumn.aop.invocation.AopMethodInvocation;
import cn.imaq.autumn.core.context.AutumnContext;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

public class AroundAdvice extends Advice {
    public AroundAdvice(AutumnContext autumnContext, String expression, Method adviceMethod) {
        super(autumnContext, expression, adviceMethod);

        if (adviceMethod.getParameterCount() != 1 || !adviceMethod.getParameterTypes()[0].isAssignableFrom(AopMethodInvocation.class)) {
            throw new IllegalArgumentException("Around advice method [" + adviceMethod + "] should have only one Joinpoint parameter");
        }
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        return invokeAdvice(invocation);
    }
}
