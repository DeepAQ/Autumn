package cn.imaq.autumn.core.context;

import cn.imaq.autumn.core.beans.BeanInfo;
import cn.imaq.autumn.core.beans.scanner.BeanScanners;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
public class AutumnContext {
    private Queue<BeanInfo> beanInfos = new ConcurrentLinkedQueue<>();
    private Map<String, BeanInfo> beansByName = new ConcurrentHashMap<>();
    private Map<Class<?>, BeanInfo> beansByType = new ConcurrentHashMap<>();

    private String name;
    private volatile boolean scanned = false;

    public AutumnContext(String name) {
        this.name = name;
    }

    public synchronized void scanComponents(String[] spec) {
        if (scanned) {
            return;
        }
        List<String> scannerSpecs = new ArrayList<>();
        FastClasspathScanner scanner = new FastClasspathScanner(spec);
        BeanScanners.processAll(scanner, this);
        log.info("Context [" + name + "] scanning components with spec " + Arrays.toString(spec));
        scanner.scan();
        scanned = true;
    }

    public void addBeanInfo(BeanInfo info) {
        log.info("Context [" + name + "] adding " + info);
        beanInfos.add(info);
        beansByType.put(info.getType(), info);
        if (!info.getName().isEmpty()) {
            beansByName.put(info.getName(), info);
        }
    }
}
