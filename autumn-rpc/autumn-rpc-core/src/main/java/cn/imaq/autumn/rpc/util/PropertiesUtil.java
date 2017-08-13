package cn.imaq.autumn.rpc.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Properties;

@Slf4j
public class PropertiesUtil {
    public static void load(Properties properties, String defaultFile, String extendFile) throws IOException {
        if (defaultFile != null) {
            log.info("Loading config from " + defaultFile);
            properties.load(PropertiesUtil.class.getClassLoader().getResourceAsStream(defaultFile));
        }
        if (extendFile != null) {
            log.info("Loading config from " + extendFile);
            properties.load(PropertiesUtil.class.getClassLoader().getResourceAsStream(extendFile));
        }
    }
}
