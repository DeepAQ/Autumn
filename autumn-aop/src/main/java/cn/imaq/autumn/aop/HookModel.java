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

    public HookModel(String[] includes, String[] excludes) {
        this.includes = Arrays.stream(includes).map(JoinPoint::new).collect(Collectors.toList());
        this.excludes = Arrays.stream(excludes).map(JoinPoint::new).collect(Collectors.toList());
    }

    public boolean matches(Method method) {
        String className = method.getDeclaringClass().getName();
        String methodName = method.getName();
        for (JoinPoint excludePoint : excludes) {
            if (excludePoint.matches(className, methodName)) {
                return false;
            }
        }
        for (JoinPoint includePoint : includes) {
            if (includePoint.matches(className, methodName)) {
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

        boolean matches(String targetClassName, String targetMethodName) {
            if (classWildcard) {
                if (!targetClassName.startsWith(this.className)) {
                    return false;
                }
            } else if (!targetClassName.equals(this.className)) {
                return false;
            }
            if (methodWildcard) {
                if (!targetMethodName.startsWith(this.methodName)) {
                    return false;
                }
            } else if (!targetMethodName.equals(this.methodName)) {
                return false;
            }
            return true;
        }
    }
}
