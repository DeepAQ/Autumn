package com.example.test;

import cn.imaq.autumn.rpc.server.annotation.AutumnRPCExpose;

@AutumnRPCExpose
public class TestServiceImpl implements TestService {
    @Override
    public String echo(String input) {
        return "Echo: " + input;
    }
}
