package cn.imaq.autumn.rpc.client.net;

import cn.imaq.autumn.rpc.client.exception.AutumnHttpException;

public interface AutumnHttpClient {
    default byte[] get(String url, int timeout) throws AutumnHttpException {
        return post(url, null, null, timeout);
    }

    byte[] post(String url, byte[] payload, String mime, int timeout) throws AutumnHttpException;
}
