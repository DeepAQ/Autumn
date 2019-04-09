package cn.imaq.autumn.aop;

import lombok.Getter;
import org.aopalliance.aop.Advice;
import org.aspectj.weaver.tools.PointcutExpression;
import org.aspectj.weaver.tools.PointcutParameter;
import org.aspectj.weaver.tools.PointcutParser;
import org.aspectj.weaver.tools.PointcutPrimitive;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HookModel implements Advice {
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

    @Getter
    private Method hook;

    public HookModel(String expression, Method hook) {
        Class<?>[] paramTypes = hook.getParameterTypes();
        if (paramTypes.length == 0 || (paramTypes.length == 1 && paramTypes[0].isAssignableFrom(HookChain.class))) {
            this.hook = hook;
        } else {
            throw new IllegalArgumentException("Hook method [" + hook + "] should have no parameters or one Joinpoint parameter");
        }

        this.pointcutExpression = PointcutParser
                .getPointcutParserSupportingSpecifiedPrimitivesAndUsingContextClassloaderForResolution(SUPPORTED_PRIMITIVES)
                .parsePointcutExpression(expression, hook.getDeclaringClass(), new PointcutParameter[0]);
    }

    public boolean matches(Class<?> clazz) {
        return pointcutExpression.couldMatchJoinPointsInType(clazz);
    }

    public boolean matches(Method method) {
        return methodMatchCache.computeIfAbsent(method, m -> pointcutExpression.matchesMethodExecution(m).alwaysMatches());
    }
}
