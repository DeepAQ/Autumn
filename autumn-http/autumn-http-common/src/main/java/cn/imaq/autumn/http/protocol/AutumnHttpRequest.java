package cn.imaq.autumn.http.protocol;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class AutumnHttpRequest {
    private String method;

    private String path;

    private String protocol;

    private Map<String, List<String>> headers;

    private byte[] body;
}
