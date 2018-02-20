package cn.imaq.autumn.core.beans.scanner;

import cn.imaq.autumn.core.context.AutumnContext;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class BeanScanners {
    private static volatile List<BeanScanner> scanners;

    public static void processAll(FastClasspathScanner scanner, AutumnContext context) {
        if (scanners == null) {
            synchronized (BeanScanners.class) {
                if (scanners == null) {
                    log.info("Init bean scanners ...");
                    scanners = new ArrayList<>();
                    new FastClasspathScanner().matchClassesImplementing(BeanScanner.class, scls -> {
                        try {
                            scanners.add(scls.newInstance());
                        } catch (Exception e) {
                            log.warn("Cannot init bean scanner [" + scls.getName() + "], " + e);
                        }
                    }).scan();
                }
            }
        }
        scanners.forEach(sc -> sc.process(scanner, context));
    }
}
