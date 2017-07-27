package cn.imaq.autumn.rpc.net;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AutumnRPCRequest {
    private String methodName;

    private Class[] paramTypes;

    private Object[] params;
}
