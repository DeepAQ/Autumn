package cn.imaq.autumn.restexample;

import cn.imaq.tompuss.TomPuss;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class Main {
    public static void main(String[] args) {
        long startTime = System.nanoTime();
        TomPuss tomPuss = new TomPuss(8081, new File(Main.class.getResource("/").getFile() + "/../resources/"));
        tomPuss.start();
        long elapsedNanos = System.nanoTime() - startTime;
        log.info("Boot time: {}ms", elapsedNanos / 1E6);
    }
}
