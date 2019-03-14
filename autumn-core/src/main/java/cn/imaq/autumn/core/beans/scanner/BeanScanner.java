package cn.imaq.autumn.core.beans.scanner;

import cn.imaq.autumn.core.context.AutumnContext;
import cn.imaq.autumn.cpscan.ScanResult;

public interface BeanScanner {
    void process(ScanResult result, AutumnContext context);
}
