package cn.imaq.autumn.rpc.server.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigUtil {
    private static final Properties config = new Properties();
    private static final String DEFAULT_CONFIG = "autumn-rpc-default.properties";

    private static void loadConfigFile(String filename) {
        synchronized (config) {
            LogUtil.I("Loading config from " + filename);
            InputStream is = ConfigUtil.class.getClassLoader().getResourceAsStream(filename);
            try {
                config.load(is);
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
                LogUtil.E("Error loading config from " + filename);
            }
        }
    }

    public static void loadConfig(String filename) {
        synchronized (config) {
            // Clear and load default config first
            config.clear();
            loadConfigFile(DEFAULT_CONFIG);
            if (filename != null && !filename.isEmpty()) {
                loadConfigFile(filename);
            }
        }
    }

    public static String get(String key) {
        return config.getProperty(key);
    }
}
