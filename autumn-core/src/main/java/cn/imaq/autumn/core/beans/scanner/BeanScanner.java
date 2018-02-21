package cn.imaq.autumn.core.beans.scanner;

import cn.imaq.autumn.core.context.AutumnContext;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanSpec;

public interface BeanScanner {
    void process(ScanSpec spec, AutumnContext context);
}
