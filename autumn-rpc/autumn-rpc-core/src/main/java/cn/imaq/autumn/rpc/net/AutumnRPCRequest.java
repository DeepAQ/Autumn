package cn.imaq.autumn.rpc.net;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class AutumnRPCRequest {
    private String methodName;

    private Class[] paramTypes;

    private JsonNode[] params;
}
