package cn.imaq.autumn.rpc.server.handler;

import cn.imaq.autumn.rpc.server.net.RpcHttpRequest;
import cn.imaq.autumn.rpc.server.net.RpcHttpResponse;

public interface RpcRequestHandler {
    RpcHttpResponse handle(RpcHttpRequest request);
}
