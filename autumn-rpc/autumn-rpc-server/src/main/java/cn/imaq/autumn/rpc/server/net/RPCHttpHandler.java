package cn.imaq.autumn.rpc.server.net;

public interface RPCHttpHandler {
    RPCHttpResponse handle(RPCHttpRequest request);
}
