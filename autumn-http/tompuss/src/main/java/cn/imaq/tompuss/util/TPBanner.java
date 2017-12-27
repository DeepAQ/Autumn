package cn.imaq.tompuss.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TPBanner {
    private static final String banner = "\n" +
            "___________           __________\n" +
            "\\__    ___/___   _____\\______   \\__ __  ______ ______\n" +
            "  |    | /  _ \\ /     \\|     ___/  |  \\/  ___//  ___/\n" +
            "  |    |(  <_> )  Y Y  \\    |   |  |  /\\___ \\ \\___ \\\n" +
            "  |____| \\____/|__|_|  /____|   |____//____  >____  >\n" +
            "                     \\/                    \\/     \\/\n" +
            "  :: TomPuss Application Server ::\n";

    public static void printBanner() {
        log.info(banner);
    }
}
