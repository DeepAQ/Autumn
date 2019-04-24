package com.example.test;

import cn.imaq.autumn.rpc.server.annotation.AutumnRPCInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

@AutumnRPCInterface
public interface TestService extends Remote {
    String echo(String input) throws RemoteException;

    MyObject testEnum(int num) throws RemoteException;

    String testObject(String str, MyObject object) throws RemoteException;

    String testArray(Object[] arr) throws RemoteException;

    List<MyObject> testList(List<MyObject> list) throws RemoteException;

    void testThrowException(String msg) throws Exception;

    Exception testReturnException(String msg) throws RemoteException;
}
