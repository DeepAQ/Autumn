package cn.imaq.autumn.resttest;

import cn.imaq.autumn.http.client.AutumnHttpClient;
import cn.imaq.autumn.http.protocol.AutumnHttpResponse;

import java.io.IOException;

public class TestUtils {
    public static AutumnHttpResponse get(String relPath) throws IOException {
        return AutumnHttpClient.get("http://127.0.0.1:8081" + relPath, 1000);
    }
}
