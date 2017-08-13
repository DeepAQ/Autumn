package com.example.test;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface TestService extends Remote {
    String echo(String input) throws RemoteException;

    MyObject testEnum(int num) throws RemoteException;

    String testObject(String str, MyObject object) throws RemoteException;

    String testArray(Object[] arr) throws RemoteException;

    String testList(List<MyObject> list) throws RemoteException;

    void testThrowException(String msg) throws Exception;

    Exception testReturnException(String msg) throws RemoteException;
}
