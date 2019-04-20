package cn.imaq.autumn.rpc.test;

import cn.imaq.autumn.core.annotation.Component;
import cn.imaq.autumn.rpc.intergration.annotation.AutumnRPCRemote;
import com.example.test.TestService;
import lombok.Getter;

@Component
public class TestServiceWrapper {
    @Getter
    @AutumnRPCRemote
    private TestService testService;
}
