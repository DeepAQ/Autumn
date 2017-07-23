package cn.imaq.autumn.rpc.server.net;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
class AutumnRPCRequest {
    private String methodName;

    private Class[] paramTypes;

    private JsonNode[] params;
}
