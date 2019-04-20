package cn.imaq.autumn.rpc.test;

import cn.imaq.autumn.core.annotation.BeanFactory;
import cn.imaq.autumn.core.annotation.Component;
import cn.imaq.autumn.rpc.client.AutumnRPCClient;
import cn.imaq.autumn.rpc.server.AutumnRPC;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

@Component
public class TestConfig {
    @BeforeSuite
    public void startServer() {
        AutumnRPC.start();
    }

    @AfterSuite
    public void stopServer() {
        AutumnRPC.stop();
    }

    @BeanFactory
    public AutumnRPCClient getRpcClient() {
        return new AutumnRPCClient("127.0.0.1", 8801);
    }
}
