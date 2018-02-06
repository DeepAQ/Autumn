package cn.imaq.autumn.rest.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class IOUtil {
    public static byte[] readInputStream(InputStream is) throws IOException {
        int size = is.available();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buf;
        if (size > 0) {
            buf = new byte[size];
        } else {
            buf = new byte[1024];
        }
        int read;
        while ((read = is.read(buf)) > 0) {
            os.write(buf, 0, read);
        }
        is.close();
        return os.toByteArray();
    }
}
