package cn.imaq.autumn.rpc.server.net;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AutumnHttpRequest {
    private String method;

    private String path;

    private Map<String, String> headers;

    private byte[] body;
}
