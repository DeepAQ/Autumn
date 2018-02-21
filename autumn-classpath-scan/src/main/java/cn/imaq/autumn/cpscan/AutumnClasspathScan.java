package cn.imaq.autumn.cpscan;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AutumnClasspathScan {
    private static volatile ScanResult scanResult;

    public static ScanResult getScanResult() {
        if (scanResult == null) {
            synchronized (AutumnClasspathScan.class) {
                if (scanResult == null) {
                    log.info("Scanning classpath ...");
                    scanResult = new FastClasspathScanner().scan();
                }
            }
        }
        return scanResult;
    }
}
