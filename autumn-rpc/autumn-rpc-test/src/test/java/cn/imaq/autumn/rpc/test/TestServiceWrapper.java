package cn.imaq.autumn.rpc.test;

import cn.imaq.autumn.core.annotation.Component;
import cn.imaq.autumn.rpc.intergration.annotation.AutumnRPCClusterRemote;
import com.example.test.TestService;
import lombok.Getter;

@Component
public class TestServiceWrapper {
    @AutumnRPCClusterRemote
    @Getter
    private TestService testService;
}
