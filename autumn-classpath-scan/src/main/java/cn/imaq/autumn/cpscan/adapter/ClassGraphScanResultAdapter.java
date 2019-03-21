package cn.imaq.autumn.cpscan.adapter;

import cn.imaq.autumn.cpscan.ScanResult;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.FieldInfo;
import io.github.classgraph.MethodInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

public class ClassGraphScanResultAdapter implements ScanResult {
    private io.github.classgraph.ScanResult cgResult;

    public ClassGraphScanResultAdapter(io.github.classgraph.ScanResult cgResult) {
        this.cgResult = cgResult;
    }

    @Override
    public List<Class<?>> getSubClassesOf(Class<?> baseClass) {
        return cgResult.getSubclasses(baseClass.getName()).loadClasses();
    }

    @Override
    public List<Class<?>> getSuperClassesOf(Class<?> baseClass) {
        return cgResult.getSuperclasses(baseClass.getName()).loadClasses();
    }

    @Override
    public List<Class<?>> getClassesImplementing(Class<?> interfaze) {
        return cgResult.getClassesImplementing(interfaze.getName())
                .filter(ClassInfo::isStandardClass)
                .loadClasses();
    }

    @Override
    public List<Class<?>> getClassesWithAnnotation(Class<? extends Annotation> annotationClass) {
        return cgResult.getClassesWithAnnotation(annotationClass.getName()).directOnly().loadClasses();
    }

    @Override
    public List<Class<?>> getClassesWithMetaAnnotation(Class<? extends Annotation> annotationClass) {
        return cgResult.getClassesWithAnnotation(annotationClass.getName()).loadClasses();
    }

    @Override
    public List<Method> getMethodsWithAnnotation(Class<? extends Annotation> annotationClass) {
        return cgResult.getClassesWithMethodAnnotation(annotationClass.getName()).stream()
                .flatMap(ci -> ci.getDeclaredMethodInfo()
                        .filter(mi -> mi.hasAnnotation(annotationClass.getName()))
                        .stream()
                        .map(MethodInfo::loadClassAndGetMethod))
                .collect(Collectors.toList());
    }

    @Override
    public List<Field> getFieldsWithAnnotation(Class<? extends Annotation> annotationClass) {
        return cgResult.getClassesWithFieldAnnotation(annotationClass.getName()).stream()
                .flatMap(ci -> ci.getDeclaredFieldInfo()
                        .filter(fi -> fi.hasAnnotation(annotationClass.getName()))
                        .stream()
                        .map(FieldInfo::loadClassAndGetField))
                .collect(Collectors.toList());
    }
}
