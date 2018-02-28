package cn.imaq.autumn.rest.model;

import cn.imaq.autumn.rest.annotation.ControllerAdvice;
import lombok.Data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ControllerAdviceModel {
    private List<ControllerJoinPoint> includes;

    private List<ControllerJoinPoint> excludes;

    public ControllerAdviceModel(ControllerAdvice anno) {
        this.includes = Arrays.stream(anno.include()).map(ControllerJoinPoint::new).collect(Collectors.toList());
        this.excludes = Arrays.stream(anno.exclude()).map(ControllerJoinPoint::new).collect(Collectors.toList());
    }

    public ControllerAdviceModel(String className) {
        this.includes = Collections.singletonList(new ControllerJoinPoint(className));
        this.excludes = Collections.emptyList();
    }

    public boolean matches(Class<?> clazz) {
        String targetClassName = clazz.getName();
        for (ControllerJoinPoint excludePoint : excludes) {
            if (excludePoint.matchClass(targetClassName)) {
                return false;
            }
        }
        for (ControllerJoinPoint includePoint : includes) {
            if (includePoint.matchClass(targetClassName)) {
                return true;
            }
        }
        return false;
    }

    @Data
    private class ControllerJoinPoint {
        private String className;

        private boolean wildcard = false;

        ControllerJoinPoint(String desc) {
            if (desc.endsWith("*")) {
                className = desc.substring(0, desc.length() - 1);
                wildcard = true;
            } else {
                className = desc;
            }
        }

        boolean matchClass(String targetClassName) {
            if (wildcard) {
                return targetClassName.startsWith(className);
            } else {
                return targetClassName.equals(className);
            }
        }
    }
}
