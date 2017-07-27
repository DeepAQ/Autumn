package cn.imaq.autumn.rpc.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AutumnRPCBanner {
    private static final String banner = "\n" +
            "                _                         _____  _____   _____ \n" +
            "     /\\        | |                       |  __ \\|  __ \\ / ____|\n" +
            "    /  \\  _   _| |_ _   _ _ __ ___  _ __ | |__) | |__) | |     \n" +
            "   / /\\ \\| | | | __| | | | '_ ` _ \\| '_ \\|  _  /|  ___/| |     \n" +
            "  / ____ \\ |_| | |_| |_| | | | | | | | | | | \\ \\| |    | |____ \n" +
            " /_/    \\_\\__,_|\\__|\\__,_|_| |_| |_|_| |_|_|  \\_\\_|     \\_____|\n" +
            "  :: AutumnRPC Server ::\n";

    public static void printBanner() {
        log.info(banner);
    }
}
