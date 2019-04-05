package cn.imaq.autumn.aop;

import lombok.Getter;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.Joinpoint;
import org.aspectj.weaver.tools.*;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class HookModel implements Advice {
    private static final Set<PointcutPrimitive> SUPPORTED_PRIMITIVES = new HashSet<PointcutPrimitive>() {{
        add(PointcutPrimitive.EXECUTION);
        add(PointcutPrimitive.WITHIN);
        add(PointcutPrimitive.ARGS);
        add(PointcutPrimitive.AT_ANNOTATION);
        add(PointcutPrimitive.AT_WITHIN);
        add(PointcutPrimitive.AT_ARGS);
    }};

    private PointcutExpression pointcutExpression;

    @Getter
    private Method hook;

    public HookModel(String expression, Method hook) {
        Class<?>[] paramTypes = hook.getParameterTypes();
        if (paramTypes.length == 0 || (paramTypes.length == 1 && Joinpoint.class.isAssignableFrom(paramTypes[0]))) {
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
        ShadowMatch match = pointcutExpression.matchesMethodExecution(method);
        return match.alwaysMatches();
    }
}
