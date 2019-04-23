package cn.imaq.autumn.rpc.registry.test;

import cn.imaq.autumn.rpc.registry.ServiceProviderEntry;
import cn.imaq.autumn.rpc.registry.ServiceRegistry;
import cn.imaq.autumn.rpc.registry.etcd3.Etcd3RegistryConfig;
import cn.imaq.autumn.rpc.registry.etcd3.Etcd3ServiceRegistry;
import cn.imaq.autumn.rpc.registry.exception.RpcRegistryException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.UUID;

public class Etcd3RegistryTest {
    private ServiceRegistry registry;

    @BeforeClass
    public void init() throws RpcRegistryException {
        registry = new Etcd3ServiceRegistry(Etcd3RegistryConfig.builder()
                .endpoints(new String[]{"http://127.0.0.1:2379"})
                .build());
        registry.start();
    }

    @AfterClass
    public void destroy() throws RpcRegistryException {
        registry.stop();
    }

    @Test
    public void test() throws RpcRegistryException, InterruptedException {
        String randStr = UUID.randomUUID().toString();
        registry.register(ServiceProviderEntry.builder()
                .serviceName("test1")
                .host("host1")
                .port(8801)
                .configStr(randStr)
                .build());
        registry.register(ServiceProviderEntry.builder()
                .serviceName("test1")
                .host("host2")
                .port(8801)
                .configStr(randStr)
                .build());
        registry.register(ServiceProviderEntry.builder()
                .serviceName("test2")
                .host("host3")
                .port(8801)
                .configStr(randStr)
                .build());

        registry.subscribe("test1");
        registry.subscribe("test2");

        Collection<ServiceProviderEntry> providers1 = registry.lookup("test1");
        Collection<ServiceProviderEntry> providers2 = registry.lookup("test2");
        System.out.println(providers1);
        System.out.println(providers2);
        Assert.assertEquals(providers1.size(), 2);
        Assert.assertEquals(providers2.size(), 1);

        registry.deregister(ServiceProviderEntry.builder()
                .serviceName("test1")
                .host("host1")
                .port(8801)
                .configStr(randStr)
                .build());
        registry.deregister(ServiceProviderEntry.builder()
                .serviceName("test2")
                .host("host3")
                .port(8801)
                .configStr(randStr)
                .build());

        Thread.sleep(2000); // wait for replication

        providers1 = registry.lookup("test1");
        providers2 = registry.lookup("test2");
        System.out.println(providers1);
        System.out.println(providers2);
        Assert.assertEquals(providers1.size(), 1);
        Assert.assertEquals(providers2.size(), 0);
    }
}
