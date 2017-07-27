package cn.imaq.autumn.rpc.test;

import cn.imaq.autumn.rpc.server.AutumnRPCServer;

public class AutumnMain {
    public static void main(String[] args) {
        AutumnRPCServer.start("autumn-rpc-test.properties");
    }
}
