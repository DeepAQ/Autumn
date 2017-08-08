package cn.imaq.autumn.http.server.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AutumnHTTPBanner {
    private static final String banner = "\n" +
            "                _                         _    _ _______ _______ _____\n" +
            "     /\\        | |                       | |  | |__   __|__   __|  __ \\\n" +
            "    /  \\  _   _| |_ _   _ _ __ ___  _ __ | |__| |  | |     | |  | |__) |\n" +
            "   / /\\ \\| | | | __| | | | '_ ` _ \\| '_ \\|  __  |  | |     | |  |  ___/\n" +
            "  / ____ \\ |_| | |_| |_| | | | | | | | | | |  | |  | |     | |  | |\n" +
            " /_/    \\_\\__,_|\\__|\\__,_|_| |_| |_|_| |_|_|  |_|  |_|     |_|  |_|\n" +
            "  :: Autumn HTTP Server ::\n";

    public static void printBanner() {
        log.info(banner);
    }
}
