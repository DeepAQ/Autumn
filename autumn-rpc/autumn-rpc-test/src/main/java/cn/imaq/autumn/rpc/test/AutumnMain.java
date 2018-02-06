package cn.imaq.autumn.rpc.test;

import cn.imaq.autumn.rpc.client.AutumnRPCClient;
import cn.imaq.autumn.rpc.server.AutumnRPCServer;
import com.example.test.MyEnum;
import com.example.test.MyObject;
import com.example.test.TestService;

import java.rmi.RemoteException;
import java.util.Arrays;

public class AutumnMain {
    public static void main(String[] args) throws RemoteException {
        AutumnRPCServer.start("autumn-rpc-test.properties");
        AutumnRPCClient client = new AutumnRPCClient("127.0.0.1", 8801, "autumn-rpc-test-client.properties", true);
        TestService testService = client.getService(TestService.class);
        // TESTS
        System.out.println(testService.echo("Hello World!"));
        System.out.println(testService.testEnum(1));
        System.out.println(testService.testObject("Hello", new MyObject(MyEnum.D)));
        System.out.println(testService.testArray(new Object[]{null, null, null}));
        System.out.println(testService.testList(Arrays.asList(new MyObject(MyEnum.A), new MyObject(MyEnum.B), new MyObject(MyEnum.C), new MyObject(MyEnum.D))).get(0).getMyEnum());
        try {
            testService.testThrowException("Throw exception test");
        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println(testService.testReturnException("Return exception test"));
    }
}
