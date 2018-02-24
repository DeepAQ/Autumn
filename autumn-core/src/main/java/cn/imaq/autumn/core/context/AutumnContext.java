package cn.imaq.autumn.core.context;

import cn.imaq.autumn.core.beans.BeanInfo;
import cn.imaq.autumn.core.beans.BeanWrapper;
import cn.imaq.autumn.core.beans.creator.BeanCreator;
import cn.imaq.autumn.core.beans.processor.AfterBeanCreateProcessor;
import cn.imaq.autumn.core.beans.processor.AfterBeanPopulateProcessor;
import cn.imaq.autumn.core.beans.processor.BeanProcessors;
import cn.imaq.autumn.core.beans.populator.BeanPopulators;
import cn.imaq.autumn.core.beans.scanner.BeanScanners;
import cn.imaq.autumn.core.exception.BeanCreationException;
import cn.imaq.autumn.core.exception.BeanPopulationException;
import cn.imaq.autumn.cpscan.AutumnClasspathScan;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanSpec;
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
    private Map<BeanInfo, Object> populatingBeans = new ConcurrentHashMap<>();
    private Map<BeanInfo, Object> singletons = new ConcurrentHashMap<>();

    private Map<String, Object> attributes = new ConcurrentHashMap<>();

    public AutumnContext(String name) {
        this.name = name;
    }

    public AutumnContext(String name, AutumnContext parent) {
        this.name = name;
        this.parent = parent;
    }

    public synchronized void scanComponents(String... specArgs) {
        if (scanned) {
            return;
        }
        ScanSpec spec = new ScanSpec(specArgs, null);
        BeanScanners.processAll(spec, this);
        log.info(formatLog("scanning components with spec {}"), Arrays.toString(specArgs));
        AutumnClasspathScan.processSpec(spec);
        scanned = true;
    }

    public void addBeanInfo(BeanInfo info) {
        log.info(formatLog("adding {}"), info);
        if (beansByType.containsKey(info.getType())) {
            log.warn(formatLog("already has a bean of type {}, ignoring the new one"), info.getType().getName());
            return;
        }
        beansByType.put(info.getType(), info);
        if (info.getName() != null && !info.getName().isEmpty()) {
            if (beansByName.containsKey(info.getName())) {
                log.warn(formatLog("already has a bean with name [{}], ignoring the new one"), info.getName());
                return;
            }
            beansByName.put(info.getName(), info);
        }
        beanInfos.add(info);
    }

    public Object getBeanByName(String name) {
        return getBeanByName(name, false);
    }

    public Object getBeanByName(String name, boolean populating) {
        BeanInfo info = findBeanInfoByName(name);
        if (info == null && parent != null) {
            return parent.getBeanByName(name, populating);
        }
        return getBeanByInfo(info, populating);
    }

    public Object getBeanByType(Class<?> type) {
        return getBeanByType(type, false);
    }

    public Object getBeanByType(Class<?> type, boolean populating) {
        BeanInfo info = findBeanInfoByType(type);
        if (info == null && parent != null) {
            return parent.getBeanByType(type, populating);
        }
        return getBeanByInfo(info, populating);
    }

    private Object getBeanByInfo(BeanInfo info, boolean populating) {
        if (info == null) {
            return null;
        }
        if (info.isSingleton()) {
            synchronized (info) {
                return getBeanByInfoInternal(info, populating);
            }
        }
        return getBeanByInfoInternal(info, populating);
    }

    private Object getBeanByInfoInternal(BeanInfo info, boolean populating) {
        if (populating && info.isSingleton() && populatingBeans.containsKey(info)) {
            return populatingBeans.get(info);
        }
        if (info.isSingleton()) {
            if (populating && populatingBeans.containsKey(info)) {
                return populatingBeans.get(info);
            } else if (singletons.containsKey(info)) {
                return singletons.get(info);
            }
        }
        try {
            return createAndPopulateBean(info);
        } catch (BeanCreationException e) {
            log.error(formatLog("error creating {}: {}"), info, String.valueOf(e));
        } catch (BeanPopulationException e) {
            log.error(formatLog("error populating {}: {}"), info, String.valueOf(e));
        }
        return null;
    }

    private Object createAndPopulateBean(BeanInfo info) throws BeanCreationException, BeanPopulationException {
        BeanCreator creator = info.getCreator();
        BeanWrapper wrapper = new BeanWrapper(this, info, creator.createBean());
        BeanProcessors.get(AfterBeanCreateProcessor.class).forEach(x -> x.process(wrapper));
        Object beanInstance = wrapper.getBeanInstance();
        populatingBeans.put(info, beanInstance);
        try {
            BeanPopulators.populateBean(this, beanInstance);
            BeanProcessors.get(AfterBeanPopulateProcessor.class).forEach(x -> x.process(wrapper));
        } finally {
            populatingBeans.remove(info);
        }
        beanInstance = wrapper.getBeanInstance();
        if (info.isSingleton()) {
            singletons.put(info, beanInstance);
        }
        return beanInstance;
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
                        log.warn(formatLog("more than one bean found with type {}"), type.getName());
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
