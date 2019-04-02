package cn.imaq.autumn.resttest;

import cn.imaq.tompuss.TomPuss;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.io.File;

public class TestConfig {
    private TomPuss tomPuss;

    @BeforeSuite
    public void startServer() {
        this.tomPuss = new TomPuss(8081, new File(getClass().getResource("/WEB-INF").getFile() + "/../"));
        this.tomPuss.start();
    }

    @AfterSuite
    public void stopServer() {
        this.tomPuss.stop();
    }
}
