package com.example.test;

import java.io.Serializable;

public class MyObject implements Serializable {
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
