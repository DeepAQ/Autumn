package cn.imaq.autumn.rpc.server.util;

/**
 * Created by adn55 on 2017/7/18.
 */
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
        LogUtil.I(banner);
    }
}
