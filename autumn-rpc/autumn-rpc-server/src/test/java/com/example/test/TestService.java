package com.example.test;

public interface TestService {
    String echo(String input);

    String echo(int input);

    String echo(Integer input);

    MyObject testEnum(int num);

    String testObject(String str, MyObject object);

    void testVoid();

    void testException() throws Exception;
}
