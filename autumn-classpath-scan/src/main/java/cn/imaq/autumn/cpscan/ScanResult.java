package cn.imaq.autumn.cpscan;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public interface ScanResult {
    List<Class<?>> getSubClassesOf(Class<?> baseClass);

    List<Class<?>> getSuperClassesOf(Class<?> baseClass);

    List<Class<?>> getClassesImplementing(Class<?> interfaze);

    List<Class<?>> getClassesWithAnnotation(Class<? extends Annotation> annotationClass);

    List<Class<?>> getClassesWithMetaAnnotation(Class<? extends Annotation> annotationClass);

    List<Method> getMethodsWithAnnotation(Class<? extends Annotation> annotationClass);

    List<Field> getFieldsWithAnnotation(Class<? extends Annotation> annotationClass);
}
