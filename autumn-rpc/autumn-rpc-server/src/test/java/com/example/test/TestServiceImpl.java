package com.example.test;

import cn.imaq.autumn.rpc.server.annotation.AutumnRPCExpose;

@AutumnRPCExpose
public class TestServiceImpl implements TestService {
    @Override
    public String echo(String input) {
        return "Echo: " + input;
    }

    @Override
    public String echo(int input) {
        return "Echo int: " + input;
    }

    @Override
    public String echo(Integer input) {
        return "Echo Integer: " + input;
    }
}
