package cn.imaq.autumn.rpc.server.net;

import lombok.Data;

@Data
public class AutumnRPCRequest {
    private String methodName;

    private Class[] paramTypes;

    private Object[] params;
}
