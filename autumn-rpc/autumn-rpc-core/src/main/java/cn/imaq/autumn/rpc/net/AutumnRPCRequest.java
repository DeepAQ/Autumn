package cn.imaq.autumn.rpc.net;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

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
        this.params = new JsonNode[params.length];
        for (int i = 0; i < params.length; i++) {
            this.params[i] = mapper.valueToTree(params[i]);
        }
    }
}
