package cn.imaq.autumn.rpc.test;

import cn.imaq.autumn.rpc.server.AutumnRPCServer;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

public class TestConfig {
    @BeforeSuite
    public void startServer() {
        AutumnRPCServer.start();
    }

    @AfterSuite
    public void stopServer() {
        AutumnRPCServer.stop();
    }
}
