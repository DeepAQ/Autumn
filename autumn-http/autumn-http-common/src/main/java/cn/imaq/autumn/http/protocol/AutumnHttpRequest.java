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

    public String toRequestString() {
        StringBuilder sb = new StringBuilder();
        sb.append(method).append(' ').append(path).append(' ').append(protocol).append("\r\n");
        if (headers != null) {
            for (Map.Entry<String, List<String>> header : headers.entrySet()) {
                for (String value : header.getValue()) {
                    sb.append(header.getKey()).append(": ").append(value).append("\r\n");
                }
            }
        }
        if (body != null) {
            sb.append("Content-Length: ").append(body.length).append("\r\n");
        }
        sb.append("\r\n");
        if (body != null) {
            sb.append(new String(body));
        }
        return sb.toString();
    }
}
