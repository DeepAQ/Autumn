package cn.imaq.autumn.rpc.server.scanner;

import cn.imaq.autumn.rpc.server.util.InstanceMap;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;

public interface AutumnRPCScanner {
    void process(FastClasspathScanner classpathScanner, InstanceMap instanceMap);
}
