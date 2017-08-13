package com.example.test;

import cn.imaq.autumn.rpc.server.annotation.AutumnRPCExpose;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

@AutumnRPCExpose
public class TestServiceImpl extends UnicastRemoteObject implements TestService {
    public TestServiceImpl() throws RemoteException {
    }

    @Override
    public String echo(String input) throws RemoteException {
        return "Echo: " + input;
    }

    @Override
    public MyObject testEnum(int num) throws RemoteException {
        return new MyObject(MyEnum.values()[num % MyEnum.values().length]);
    }

    @Override
    public String testObject(String str, MyObject object) throws RemoteException {
        return str + " " + object;
    }

    @Override
    public String testArray(Object[] arr) throws RemoteException {
        return "Test array: Object[" + arr.length + "]";
    }

    @Override
    public String testList(List<MyObject> list) throws RemoteException {
        StringBuilder sb = new StringBuilder("Test list: ");
        list.forEach(o -> sb.append(o.getMyEnum()));
        return sb.toString();
    }

    @Override
    public void testThrowException(String msg) throws Exception {
        throw new Exception(msg);
    }

    @Override
    public Exception testReturnException(String msg) throws RemoteException {
        return new Exception(msg);
    }
}
