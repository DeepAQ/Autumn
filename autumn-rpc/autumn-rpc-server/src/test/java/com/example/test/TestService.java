package com.example.test;

public interface TestService {
    String echo(String input);

    String echo(int input);

    String echo(Integer input);

    MyObject testEnum(int num);

    void testVoid();

    void testException() throws Exception;
}
