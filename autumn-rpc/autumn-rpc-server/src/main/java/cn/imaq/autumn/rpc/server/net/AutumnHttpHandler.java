package cn.imaq.autumn.rpc.server.net;

public interface AutumnHttpHandler {
    RPCHttpResponse handle(RPCHttpRequest request);
}
