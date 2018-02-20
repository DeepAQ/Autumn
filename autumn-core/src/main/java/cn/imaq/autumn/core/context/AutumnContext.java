package cn.imaq.autumn.core.context;

import cn.imaq.autumn.core.beans.BeanInfo;
import cn.imaq.autumn.core.beans.creator.BeanCreator;
import cn.imaq.autumn.core.beans.scanner.BeanScanners;
import cn.imaq.autumn.core.exception.BeanCreationException;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
public class AutumnContext {
    private String name;
    private AutumnContext parent;
    private volatile boolean scanned = false;

    private Queue<BeanInfo> beanInfos = new ConcurrentLinkedQueue<>();
    private Map<String, BeanInfo> beansByName = new ConcurrentHashMap<>();
    private Map<Class<?>, BeanInfo> beansByType = new ConcurrentHashMap<>();
    private Map<BeanInfo, Object> singletons = new ConcurrentHashMap<>();

    private Map<String, Object> attributes = new ConcurrentHashMap<>();

    public AutumnContext(String name) {
        this.name = name;
    }

    public AutumnContext(String name, AutumnContext parent) {
        this.name = name;
        this.parent = parent;
    }

    public synchronized void scanComponents(String... spec) {
        if (scanned) {
            return;
        }
        FastClasspathScanner scanner = new FastClasspathScanner(spec);
        BeanScanners.processAll(scanner, this);
        log.info(formatLog("scanning components with spec " + Arrays.toString(spec)));
        scanner.scan();
        scanned = true;
    }

    public void addBeanInfo(BeanInfo info) {
        log.info(formatLog("adding " + info));
        if (beansByType.containsKey(info.getType())) {
            log.warn(formatLog("already has a bean of type " + info.getType().getName() + ", ignoring the new one"));
            return;
        }
        beansByType.put(info.getType(), info);
        if (info.getName() != null && !info.getName().isEmpty()) {
            if (beansByName.containsKey(info.getName())) {
                log.warn(formatLog("already has a bean with name [" + info.getName() + "], ignoring the new one"));
                return;
            }
            beansByName.put(info.getName(), info);
        }
        beanInfos.add(info);
    }

    public Object getBeanByName(String name) {
        BeanInfo info = findBeanInfoByName(name);
        if (info == null && parent != null) {
            return parent.getBeanByName(name);
        }
        return getBeanByInfo(info);
    }

    public Object getBeanByType(Class<?> type) {
        BeanInfo info = findBeanInfoByType(type);
        if (info == null && parent != null) {
            return parent.getBeanByType(type);
        }
        return getBeanByInfo(info);
    }

    private Object getBeanByInfo(BeanInfo info) {
        if (info == null) {
            return null;
        }
        if (info.isSingleton() && singletons.containsKey(info)) {
            return singletons.get(info);
        }
        try {
            return createAndPopulateBean(info);
        } catch (BeanCreationException e) {
            log.error(formatLog("error creating " + info + ": " + e));
            return null;
        }
    }

    private Object createAndPopulateBean(BeanInfo info) throws BeanCreationException {
        BeanCreator creator = info.getCreator();
        Object beanInstance = creator.createBean();
        if (info.isSingleton()) {
            singletons.put(info, beanInstance);
        }
        return beanInstance;
        // TODO populating
    }

    private BeanInfo findBeanInfoByName(String name) {
        return beansByName.get(name);
    }

    private BeanInfo findBeanInfoByType(Class<?> type) {
        BeanInfo result = beansByType.get(type);
        if (result == null) {
            for (BeanInfo info : beanInfos) {
                if (type.isAssignableFrom(info.getType())) {
                    if (result != null) {
                        log.warn(formatLog("more than one bean found with type " + type.getName()));
                        return null;
                    }
                    result = info;
                }
            }
            if (result != null) {
                beansByType.put(type, result);
            }
        }
        return result;
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    private String formatLog(String msg) {
        return "Context [" + name + "] " + msg;
    }
}
