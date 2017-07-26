package cn.imaq.autumn.rpc.net;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

@Data
public class AutumnRPCResponse {
    private int status;

    private JsonNode result;

    private Class resultType;

    public AutumnRPCResponse() {
    }

    public AutumnRPCResponse(int status, Object result, ObjectMapper mapper) {
        this.status = status;
        this.result = mapper.valueToTree(result);
        this.resultType = result.getClass();
    }
}
