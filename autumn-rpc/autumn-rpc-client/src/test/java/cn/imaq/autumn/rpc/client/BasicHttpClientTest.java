package cn.imaq.autumn.rpc.client;

import cn.imaq.autumn.rpc.client.exception.AutumnHttpException;
import cn.imaq.autumn.rpc.client.net.BasicHttpClient;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BasicHttpClientTest {
    @Test
    public void testPost() throws UnsupportedEncodingException, AutumnHttpException {
        byte[] result = new BasicHttpClient().post(
                "https://posttestserver.com/post.php",
                "Test from Autumn".getBytes("UTF-8"),
                "text/plain", 5000
        );
        assertNotNull(result);
        assertTrue(result.length > 0);
        System.out.println(new String(result));
    }
}
