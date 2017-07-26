package com.example.test;

public class MyObject {
    private MyEnum myEnum;

    public MyObject() {
    }

    public MyObject(MyEnum myEnum) {
        this.myEnum = myEnum;
    }

    public MyEnum getMyEnum() {
        return myEnum;
    }

    public void setMyEnum(MyEnum myEnum) {
        this.myEnum = myEnum;
    }
}
