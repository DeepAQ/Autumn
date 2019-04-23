package cn.imaq.autumn.rpc.test;

import cn.imaq.autumn.rpc.cluster.AutumnRPCClusterClient;
import cn.imaq.autumn.rpc.cluster.config.RpcClusterClientConfig;
import cn.imaq.autumn.rpc.registry.etcd3.Etcd3RegistryConfig;
import cn.imaq.autumn.rpc.registry.etcd3.Etcd3ServiceRegistry;
import com.example.test.MyEnum;
import com.example.test.MyObject;
import com.example.test.TestService;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AutumnRPCTest {
    @Test
    public void test() throws Exception {
        AutumnRPCClusterClient clusterClient = new AutumnRPCClusterClient(RpcClusterClientConfig.builder()
                .registry(new Etcd3ServiceRegistry(Etcd3RegistryConfig.builder()
                        .endpoints(new String[]{"http://127.0.0.1:2379"})
                        .build()))
                .build());
        TestService testService = clusterClient.getProxy(TestService.class);

        // TESTS
        String randStr = UUID.randomUUID().toString();
        Assert.assertEquals(testService.echo(randStr), "Echo: " + randStr);

        List<MyObject> testList = Arrays.stream(MyEnum.values()).map(MyObject::new).collect(Collectors.toList());
        for (MyEnum e : MyEnum.values()) {
            Assert.assertEquals(testService.testEnum(e.ordinal()).getMyEnum(), e);
            Assert.assertEquals(testService.testList(testList).get(e.ordinal()).getMyEnum(), e);
            Assert.assertTrue(testService.testObject(randStr, new MyObject(e)).startsWith(randStr + " com.example.test.MyObject@"));
        }

        Assert.assertEquals(testService.testArray(new Object[]{null, null, null}), "Test array: Object[3]");
        try {
            testService.testThrowException(randStr);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertEquals(e.getMessage(), randStr);
        }
        Assert.assertEquals(testService.testReturnException(randStr).getMessage(), randStr);
    }
}
