package cn.imaq.autumn.rpc.test;

import cn.imaq.autumn.rpc.client.AutumnRPCClient;
import cn.imaq.autumn.rpc.server.AutumnRPCServer;
import com.example.test.TestService;

import java.rmi.RemoteException;

public class AutumnMain {
    public static void main(String[] args) throws RemoteException {
        AutumnRPCServer.start("autumn-rpc-test.properties");
        AutumnRPCClient client = new AutumnRPCClient("127.0.0.1", 8801, "autumn-rpc-test-client.properties");
        System.out.println(client.getService(TestService.class).echo("Hello World!"));
    }
}
