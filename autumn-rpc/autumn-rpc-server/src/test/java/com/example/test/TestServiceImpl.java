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

    @Override
    public MyObject testEnum(int num) {
        return new MyObject(MyEnum.values()[num % MyEnum.values().length]);
    }

    @Override
    public String testObject(String str, MyObject object) {
        return str + " " + object.toString();
    }

    @Override
    public void testVoid() {
    }

    @Override
    public void testException() throws Exception {
        throw new Exception("test exception");
    }
}
