package cn.imaq.autumn.core.beans.processor;

import cn.imaq.autumn.cpscan.AutumnClasspathScan;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@SuppressWarnings("unchecked")
public class BeanProcessors {
    private static Map<Class<? extends BeanProcessor>, List<BeanProcessor>> beanProcessors = new HashMap<>();
    private static volatile boolean init = false;

    private static void ensureInit() {
        if (!init) {
            synchronized (BeanProcessors.class) {
                if (!init) {
                    log.info("Init bean processors ...");
                    ScanResult result = AutumnClasspathScan.getScanResult();
                    result.getNamesOfClassesImplementing(BeanProcessor.class).forEach(cn -> {
                        try {
                            Class<? extends BeanProcessor> processorClass = (Class<? extends BeanProcessor>) result.classNameToClassRef(cn);
                            BeanProcessor processor = processorClass.newInstance();
                            for (Class<?> intf : processorClass.getInterfaces()) {
                                if (BeanProcessor.class.isAssignableFrom(intf)) {
                                    beanProcessors.computeIfAbsent((Class<? extends BeanProcessor>) intf, x -> new ArrayList<>()).add(processor);
                                }
                            }
                        } catch (Exception e) {
                            log.warn("Cannot init bean processor [" + cn + "]: " + e);
                        }
                    });
                    init = true;
                }
            }
        }
    }

    public static <T extends BeanProcessor> List<T> get(Class<T> processorClass) {
        ensureInit();
        List<T> processors = (List<T>) beanProcessors.get(processorClass);
        if (processors == null) {
            return Collections.emptyList();
        }
        return processors;
    }
}
