package cn.imaq.autumn.rpc.net;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.util.Arrays;

@Data
public class AutumnRPCRequest {
    private String methodName;

    private Class[] paramTypes;

    private JsonNode[] params;

    public AutumnRPCRequest() {
    }

    public AutumnRPCRequest(String methodName, Class[] paramTypes, Object[] params, ObjectMapper mapper) {
        this.methodName = methodName;
        this.paramTypes = paramTypes;
        this.params = (JsonNode[]) Arrays.stream(params).map(mapper::valueToTree).toArray();
    }
}
