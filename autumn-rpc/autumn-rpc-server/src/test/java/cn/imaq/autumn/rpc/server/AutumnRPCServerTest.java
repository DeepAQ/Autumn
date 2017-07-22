package cn.imaq.autumn.rpc.server;

import org.junit.Test;

public class AutumnRPCServerTest {
    @Test
    public void test() {
        AutumnRPCServer.start("autumn-rpc-test.properties");
    }
}
