package cn.imaq.autumn.rpc.server.net;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RpcHttpResponse {
    private int code;

    private String contentType;

    private byte[] body;
}
