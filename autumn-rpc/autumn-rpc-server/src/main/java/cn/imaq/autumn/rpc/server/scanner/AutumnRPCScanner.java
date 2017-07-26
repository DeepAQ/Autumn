package cn.imaq.autumn.rpc.server.scanner;

import cn.imaq.autumn.rpc.server.net.InstanceMap;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;

public interface AutumnRPCScanner {
    void process(FastClasspathScanner classpathScanner, InstanceMap instanceMap);
}
