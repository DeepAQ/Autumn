package cn.imaq.autumn.aop;

import lombok.Data;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class HookModel {
    private List<JoinPoint> includes;

    private List<JoinPoint> excludes;

    private Method hook;

    public HookModel(String[] includes, String[] excludes, Method hook) {
        this.includes = Arrays.stream(includes).map(JoinPoint::new).collect(Collectors.toList());
        this.excludes = Arrays.stream(excludes).map(JoinPoint::new).collect(Collectors.toList());
        this.hook = hook;
    }

    public boolean matches(Class<?> clazz) {
        String className = clazz.getName();
        for (JoinPoint includePoint : includes) {
            if (includePoint.matchesClass(className)) {
                return true;
            }
        }
        return false;
    }

    public boolean matches(Method method) {
        String className = method.getDeclaringClass().getName();
        String methodName = method.getName();
        for (JoinPoint excludePoint : excludes) {
            if (excludePoint.matchesMethod(className, methodName)) {
                return false;
            }
        }
        for (JoinPoint includePoint : includes) {
            if (includePoint.matchesMethod(className, methodName)) {
                return true;
            }
        }
        return false;
    }

    @Data
    private class JoinPoint {
        private String className;

        private boolean classWildcard = false;

        private String methodName;

        private boolean methodWildcard = false;

        JoinPoint(String desc) {
            String[] classAndMethod = desc.split("#");
            className = classAndMethod[0];
            if (className.endsWith("*")) {
                className = className.substring(0, className.length() - 1);
                classWildcard = true;
            }
            if (classAndMethod.length >= 2) {
                methodName = classAndMethod[1];
                if (methodName.endsWith("*")) {
                    methodName = methodName.substring(0, methodName.length() - 1);
                    methodWildcard = true;
                }
            } else {
                methodName = "";
                methodWildcard = true;
            }
        }

        boolean matchesClass(String targetClassName) {
            if (classWildcard) {
                return targetClassName.startsWith(this.className);
            } else return targetClassName.equals(this.className);
        }

        boolean matchesMethod(String targetClassName, String targetMethodName) {
            if (!matchesClass(targetClassName)) {
                return false;
            }
            if (methodWildcard) {
                return targetMethodName.startsWith(this.methodName);
            } else return targetMethodName.equals(this.methodName);
        }
    }
}
