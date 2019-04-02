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
    public void testHello2() throws IOException {
        AutumnHttpResponse resp = TestUtils.get("/hello/");
        Assert.assertEquals(resp.getBody(), "<h1>Hello AutumnREST!</h1>".getBytes());
    }

    @Test
    public void testHelloWithQuery() throws IOException {
        AutumnHttpResponse resp = TestUtils.get("/hello?name=World");
        Assert.assertEquals(resp.getBody(), "<h1>Hello World!</h1>".getBytes());
    }

    @Test
    public void testHelloWithQuery2() throws IOException {
        AutumnHttpResponse resp = TestUtils.get("/hello/?name=World");
        Assert.assertEquals(resp.getBody(), "<h1>Hello World!</h1>".getBytes());
    }

    @Test
    public void testHelloBytes() throws IOException {
        AutumnHttpResponse resp = TestUtils.get("/hello/bytes");
        Assert.assertEquals(resp.getBody(), "<h1>Hello AutumnREST!</h1>".getBytes());
    }

    @Test
    public void testNotFound() throws IOException {
        AutumnHttpResponse resp = TestUtils.get("/hello/world");
        Assert.assertEquals(resp.getStatus(), 404);
    }
}
