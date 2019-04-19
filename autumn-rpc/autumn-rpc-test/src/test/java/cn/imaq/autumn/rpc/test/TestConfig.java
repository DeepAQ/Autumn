package cn.imaq.autumn.rpc.test;

import cn.imaq.autumn.rpc.server.AutumnRPC;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

public class TestConfig {
    @BeforeSuite
    public void startServer() {
        AutumnRPC.start();
    }

    @AfterSuite
    public void stopServer() {
        AutumnRPC.stop();
    }
}
