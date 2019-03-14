package cn.imaq.autumn.cpscan;

import cn.imaq.autumn.cpscan.adapter.ClassGraphScanResultAdapter;
import io.github.classgraph.ClassGraph;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AutumnClasspathScan {
    private static volatile ScanResult globalResult;

    public static ScanResult getGlobalScanResult() {
        if (globalResult == null) {
            synchronized (AutumnClasspathScan.class) {
                if (globalResult == null) {
                    log.info("Scanning classpath ...");
                    globalResult = new ClassGraphScanResultAdapter(
                            new ClassGraph()
                                    .enableAllInfo()
                                    .scan()
                    );
                }
            }
        }

        return globalResult;
    }
}
