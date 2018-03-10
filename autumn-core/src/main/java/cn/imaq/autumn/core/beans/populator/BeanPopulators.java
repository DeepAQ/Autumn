package cn.imaq.autumn.core.beans.populator;

import cn.imaq.autumn.core.context.AutumnContext;
import cn.imaq.autumn.core.exception.BeanPopulationException;
import cn.imaq.autumn.cpscan.AutumnClasspathScan;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class BeanPopulators {
    private static Map<Class<? extends Annotation>, AnnotatedFieldPopulator<?>> annotatedFieldPopulatorMap = new HashMap<>();
    private static volatile boolean init = false;

    private static void ensureInit() {
        if (!init) {
            synchronized (BeanPopulators.class) {
                if (!init) {
                    log.info("Init bean populators ...");
                    ScanResult result = AutumnClasspathScan.getScanResult();
                    result.getNamesOfSubclassesOf(AnnotatedFieldPopulator.class).forEach(cn -> {
                        try {
                            AnnotatedFieldPopulator<?> populator = (AnnotatedFieldPopulator<?>) result.classNameToClassRef(cn).newInstance();
                            Class<? extends Annotation> annotationClass = populator.getAnnotationClass();
                            if (annotationClass != null) {
                                annotatedFieldPopulatorMap.put(annotationClass, populator);
                            }
                        } catch (Exception e) {
                            log.warn("Cannot init bean populator [{}]: {}", cn, String.valueOf(e));
                        }
                    });
                    init = true;
                }
            }
        }
    }

    public static void populateBean(AutumnContext context, Object instance) throws BeanPopulationException {
        ensureInit();
        // populate fields
        for (Field field : instance.getClass().getDeclaredFields()) {
            try {
                // annotated populators
                for (Class<? extends Annotation> annoClass : annotatedFieldPopulatorMap.keySet()) {
                    if (field.isAnnotationPresent(annoClass)) {
                        Object value = annotatedFieldPopulatorMap.get(annoClass).populate(context, field);
                        field.setAccessible(true);
                        field.set(instance, value);
                        break;
                    }
                }
            } catch (Exception e) {
                throw new BeanPopulationException("Unable to inject field " + field + ": " + e);
            }
        }
    }
}
