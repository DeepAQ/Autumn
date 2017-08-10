package cn.imaq.autumn.rpc.server.net;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RPCHttpResponse {
    private int code;

    private String contentType;

    private byte[] body;
}
