package cn.imaq.autumn.resttest;

import cn.imaq.autumn.http.protocol.AutumnHttpResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

public class HelloTest {
    @Test
    public void testHello() throws IOException {
        AutumnHttpResponse resp = TestUtils.get("/hello");
        Assert.assertEquals(resp.getBody(), "<h1>Hello AutumnREST!</h1>".getBytes());
    }

    @Test
    public void testHelloWithQuery() throws IOException {
        AutumnHttpResponse resp = TestUtils.get("/hello?name=World");
        Assert.assertEquals(resp.getBody(), "<h1>Hello World!</h1>".getBytes());
    }
}
