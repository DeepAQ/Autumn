package cn.imaq.autumn.resttest.rest;

import cn.imaq.autumn.http.client.AutumnHttpClient;
import cn.imaq.autumn.http.protocol.AutumnHttpRequest;
import cn.imaq.autumn.http.protocol.AutumnHttpResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ParamsTest {
    private static InetSocketAddress dest = new InetSocketAddress("127.0.0.1", 8081);

    @Test
    public void testCookies() throws IOException {
        Map<String, List<String>> headers = new HashMap<>();
        String rand1 = UUID.randomUUID().toString();
        String rand2 = UUID.randomUUID().toString();
        headers.put("Cookie", Collections.singletonList("test1=" + rand1 + "; test2=" + rand2 + ";"));
        AutumnHttpRequest request = AutumnHttpRequest.builder()
                .method("GET")
                .path("/params/cookies")
                .protocol("HTTP/1.1")
                .headers(headers)
                .build();
        AutumnHttpResponse response = AutumnHttpClient.request(request, dest, 1000);
        Assert.assertEquals(new String(response.getBody()), "[{\"name\":\"test1\",\"value\":\"" + rand1 + "\",\"comment\":null,\"domain\":null,\"maxAge\":-1,\"path\":null,\"secure\":false,\"version\":0,\"httpOnly\":false},{\"name\":\"test2\",\"value\":\"" + rand2 + "\",\"comment\":null,\"domain\":null,\"maxAge\":-1,\"path\":null,\"secure\":false,\"version\":0,\"httpOnly\":false}]");
    }

    @Test
    public void testCookieValue() throws IOException {
        Map<String, List<String>> headers = new HashMap<>();
        String rand1 = UUID.randomUUID().toString();
        String rand2 = UUID.randomUUID().toString();
        headers.put("Cookie", Collections.singletonList("test1=" + rand1 + "; test2=" + rand2 + ";"));
        AutumnHttpRequest request = AutumnHttpRequest.builder()
                .method("GET")
                .path("/params/cookieValue")
                .protocol("HTTP/1.1")
                .headers(headers)
                .build();
        AutumnHttpResponse response = AutumnHttpClient.request(request, dest, 1000);
        Assert.assertEquals(new String(response.getBody()), rand1 + "," + rand2);
    }

    @Test
    public void testBody() throws IOException {
        String rand = UUID.randomUUID().toString();
        AutumnHttpRequest request = AutumnHttpRequest.builder()
                .method("POST")
                .path("/params/body")
                .protocol("HTTP/1.1")
                .body(rand.getBytes())
                .build();
        AutumnHttpResponse response = AutumnHttpClient.request(request, dest, 1000);
        Assert.assertEquals(new String(response.getBody()), rand);
    }

    @Test
    public void testHeader() throws IOException {
        Map<String, List<String>> headers = new HashMap<>();
        String rand1 = UUID.randomUUID().toString();
        String rand2 = UUID.randomUUID().toString();
        headers.put("Test1", Collections.singletonList(rand1));
        headers.put("Test2", Collections.singletonList(rand2));
        AutumnHttpRequest request = AutumnHttpRequest.builder()
                .method("GET")
                .path("/params/header")
                .protocol("HTTP/1.1")
                .headers(headers)
                .build();
        AutumnHttpResponse response = AutumnHttpClient.request(request, dest, 1000);
        Assert.assertEquals(new String(response.getBody()), rand1 + "," + rand2);
    }

    @Test
    public void testParams() throws IOException {
        String param1 = UUID.randomUUID().toString();
        byte[] randBytes = new byte[10];
        new Random().nextBytes(randBytes);
        String param2 = new String(randBytes, StandardCharsets.UTF_8);
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Content-Type", Collections.singletonList("application/x-www-form-urlencoded"));
        AutumnHttpRequest request = AutumnHttpRequest.builder()
                .method("POST")
                .path("/params/params")
                .protocol("HTTP/1.1")
                .headers(headers)
                .body(("test1=" + param1 + "&test2=" + URLEncoder.encode(param2, "utf-8")).getBytes())
                .build();
        AutumnHttpResponse response = AutumnHttpClient.request(request, dest, 1000);
        Assert.assertEquals(new String(response.getBody(), StandardCharsets.UTF_8), param1 + "," + param2);
    }
}