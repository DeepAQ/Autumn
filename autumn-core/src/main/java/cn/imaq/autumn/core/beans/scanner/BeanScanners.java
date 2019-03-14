package cn.imaq.autumn.core.beans.scanner;

import cn.imaq.autumn.core.context.AutumnContext;
import cn.imaq.autumn.cpscan.ScanResult;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class BeanScanners {
    private static volatile List<BeanScanner> scanners;

    public static void processAll(ScanResult result, AutumnContext context) {
        if (scanners == null) {
            synchronized (BeanScanners.class) {
                if (scanners == null) {
                    log.info("Init bean scanners ...");
                    scanners = new ArrayList<>();
                    result.getClassesImplementing(BeanScanner.class).forEach(c -> {
                        try {
                            scanners.add((BeanScanner) c.newInstance());
                        } catch (Exception e) {
                            log.warn("Cannot init bean scanner [{}]: {}", c, String.valueOf(e));
                        }
                    });
                }
            }
        }

        scanners.forEach(sc -> sc.process(result, context));
    }
}
