package cn.imaq.autumn.rpc.client.net;

import cn.imaq.autumn.rpc.client.exception.AutumnHttpException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class BasicHttpClient implements RPCHttpClient {
    @Override
    public byte[] post(String url, byte[] payload, String mime, int timeout) throws AutumnHttpException {
        int respCode = -1;
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            if (payload != null) {
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", mime);
                OutputStream os = conn.getOutputStream();
                os.write(payload);
                os.close();
            }
            respCode = conn.getResponseCode();
            if (respCode == 200) {
                InputStream is = conn.getInputStream();
                byte[] buf = new byte[conn.getContentLength()];
                is.read(buf);
                is.close();
                return buf;
            }
        } catch (IOException ignored) {
        } finally {
            if (respCode != 200) {
                throw new AutumnHttpException(respCode);
            }
        }
        return new byte[0];
    }
}
