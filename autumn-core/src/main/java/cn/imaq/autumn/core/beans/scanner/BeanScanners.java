package cn.imaq.autumn.core.beans.scanner;

import cn.imaq.autumn.core.context.AutumnContext;
import cn.imaq.autumn.cpscan.AutumnClasspathScan;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanSpec;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class BeanScanners {
    private static volatile List<BeanScanner> scanners;

    public static void processAll(ScanSpec spec, AutumnContext context) {
        if (scanners == null) {
            synchronized (BeanScanners.class) {
                if (scanners == null) {
                    log.info("Init bean scanners ...");
                    scanners = new ArrayList<>();
                    ScanResult result = AutumnClasspathScan.getScanResult();
                    result.getNamesOfClassesImplementing(BeanScanner.class).forEach(cn -> {
                        try {
                            scanners.add((BeanScanner) result.classNameToClassRef(cn).newInstance());
                        } catch (Exception e) {
                            log.warn("Cannot init bean scanner [" + cn + "], " + e);
                        }
                    });
                }
            }
        }
        scanners.forEach(sc -> sc.process(spec, context));
    }
}
