package com.example.test;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TestService extends Remote {
    String echo(String input) throws RemoteException;

    String echo(int input) throws RemoteException;

    String echo(Integer input) throws RemoteException;

    MyObject testEnum(int num) throws RemoteException;

    String testObject(String str, MyObject object) throws RemoteException;

    void testVoid() throws RemoteException;

    void testException() throws Exception;
}
