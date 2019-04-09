package cn.imaq.autumn.aop.advice;

import cn.imaq.autumn.aop.exception.AopInvocationException;
import cn.imaq.autumn.core.context.AutumnContext;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.weaver.tools.PointcutExpression;
import org.aspectj.weaver.tools.PointcutParameter;
import org.aspectj.weaver.tools.PointcutParser;
import org.aspectj.weaver.tools.PointcutPrimitive;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Advice implements MethodInterceptor {
    private static final Set<PointcutPrimitive> SUPPORTED_PRIMITIVES = new HashSet<PointcutPrimitive>() {{
        add(PointcutPrimitive.EXECUTION);
        add(PointcutPrimitive.WITHIN);
        add(PointcutPrimitive.ARGS);
        add(PointcutPrimitive.AT_ANNOTATION);
        add(PointcutPrimitive.AT_WITHIN);
        add(PointcutPrimitive.AT_ARGS);
        add(PointcutPrimitive.REFERENCE);
    }};

    private PointcutExpression pointcutExpression;

    private Map<Method, Boolean> methodMatchCache = new ConcurrentHashMap<>();

    private AutumnContext autumnContext;

    protected Method adviceMethod;

    private boolean isStaticMethod;

    public Advice(AutumnContext autumnContext, String expression, Method adviceMethod) {
        this.autumnContext = autumnContext;
        this.pointcutExpression = PointcutParser
                .getPointcutParserSupportingSpecifiedPrimitivesAndUsingContextClassloaderForResolution(SUPPORTED_PRIMITIVES)
                .parsePointcutExpression(expression, adviceMethod.getDeclaringClass(), new PointcutParameter[0]);
        this.adviceMethod = adviceMethod;
        this.isStaticMethod = Modifier.isStatic(adviceMethod.getModifiers());
    }

    public boolean matches(Class<?> clazz) {
        return pointcutExpression.couldMatchJoinPointsInType(clazz);
    }

    public boolean matches(Method method) {
        return methodMatchCache.computeIfAbsent(method, m -> pointcutExpression.matchesMethodExecution(m).alwaysMatches());
    }

    public abstract Object invoke(MethodInvocation invocation) throws Throwable;

    protected Object invokeAdvice(Object... args) throws Throwable {
        try {
            if (isStaticMethod) {
                return adviceMethod.invoke(null, args);
            } else {
                Object aspectInstance = autumnContext.getBeanByType(adviceMethod.getDeclaringClass());
                if (aspectInstance == null) {
                    throw new AopInvocationException("Cannot get aspect instance for advice " + adviceMethod);
                }
                return adviceMethod.invoke(aspectInstance, args);
            }
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
