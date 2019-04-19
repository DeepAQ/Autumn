package cn.imaq.autumn.rpc.server.net;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RpcHttpRequest {
    private String method;

    private String path;

    private byte[] body;
}
