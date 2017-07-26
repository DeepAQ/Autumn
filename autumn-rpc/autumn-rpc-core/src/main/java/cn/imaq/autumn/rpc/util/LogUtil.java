package cn.imaq.autumn.rpc.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtil {
    private static final Logger logger = LoggerFactory.getLogger("AutumnRPCServer");

    public static void T(String content) {
        logger.trace(content);
    }

    public static void D(String content) {
        logger.debug(content);
    }

    public static void I(String content) {
        logger.info(content);
    }

    public static void W(String content) {
        logger.warn(content);
    }

    public static void E(String content) {
        logger.error(content);
    }
}
