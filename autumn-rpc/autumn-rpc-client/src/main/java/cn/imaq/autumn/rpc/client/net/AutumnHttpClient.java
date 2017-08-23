package cn.imaq.autumn.rpc.client.net;

import cn.imaq.autumn.http.protocol.AutumnHttpResponse;
import cn.imaq.autumn.rpc.client.exception.AutumnHttpException;

import java.io.IOException;

public class AutumnHttpClient implements RPCHttpClient {
    @Override
    public byte[] post(String url, byte[] payload, String mime, int timeout) throws AutumnHttpException {
        int respCode = -1;
        try {
            AutumnHttpResponse response = cn.imaq.autumn.http.client.AutumnHttpClient.post(url, mime, payload, timeout);
            respCode = response.getStatus();
            if (respCode == 200) {
                return response.getBody();
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
