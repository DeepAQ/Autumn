package cn.imaq.autumn.core.beans.scanner;

import cn.imaq.autumn.core.context.AutumnContext;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;

public interface BeanScanner {
    void process(FastClasspathScanner classpathScanner, AutumnContext context);
}
