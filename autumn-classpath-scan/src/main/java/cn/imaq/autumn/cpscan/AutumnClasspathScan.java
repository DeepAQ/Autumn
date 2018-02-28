package cn.imaq.autumn.cpscan;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanSpec;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AutumnClasspathScan {
    private static volatile ScanResult scanResult;

    public static ScanResult getScanResult() {
        if (scanResult == null) {
            synchronized (AutumnClasspathScan.class) {
                if (scanResult == null) {
                    log.info("Scanning classpath ...");
                    scanResult = new FastClasspathScanner()
                            .enableFieldAnnotationIndexing()
                            .enableMethodAnnotationIndexing()
                            .scan();
                }
            }
        }
        return scanResult;
    }

    public static void processSpec(ScanSpec spec) {
        spec.callMatchProcessors(getScanResult());
    }
}
