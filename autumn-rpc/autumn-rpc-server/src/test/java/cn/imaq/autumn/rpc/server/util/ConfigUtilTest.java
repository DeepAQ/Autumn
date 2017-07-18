package cn.imaq.autumn.rpc.server.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConfigUtilTest {
    @Test
    public void testLoadDefaultConfig() {
        ConfigUtil.loadConfig(null);
        assertEquals(ConfigUtil.get("http.port"), "10080");
    }

    @Test
    public void testLoadExternalConfig() {
        ConfigUtil.loadConfig("autumn-rpc-test.properties");
        assertEquals(ConfigUtil.get("http.port"), "12345");
    }
}
