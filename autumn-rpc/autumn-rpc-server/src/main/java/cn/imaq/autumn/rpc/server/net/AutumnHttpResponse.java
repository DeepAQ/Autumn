package cn.imaq.autumn.rpc.server.net;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class AutumnHttpResponse {
    private int code;

    private boolean keepAlive;

    private Map<String, String> headers;

    private String contentType;

    private byte[] body;

    public AutumnHttpResponse setExtraHeader(String key, String value) {
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put(key, value);
        return this;
    }

    public String getFullHeader() {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(code).append(' ').append(ResponseCodes.get(code)).append("\r\n");
        if (!keepAlive) {
            sb.append("Connection: close\r\n");
        }
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue());
            }
        }
        sb.append("Content-Type: ").append(contentType).append("\r\n");
        sb.append("Content-Length: ").append(body.length).append("\r\n\r\n");
        return sb.toString();
    }

    static class ResponseCodes {
        static final String[] RESPONSE_CODES = {
                "Continue",
                "Switching Protocols",
                "Processing",
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                "OK",
                "Created",
                "Accepted",
                "Non-authoritative Information",
                "No Content",
                "Reset Content",
                "Partial Content",
                "Multi-Status",
                "Already Reported",
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                "IM Used",
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                "Multiple Choices",
                "Moved Permanently",
                "Found",
                "See Other",
                "Not Modified",
                "Use Proxy",
                null,
                "Temporary Redirect",
                "Permanent Redirect",
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                "Bad Request",
                "Unauthorized",
                "Payment Required",
                "Forbidden",
                "Not Found",
                "Method Not Allowed",
                "Not Acceptable",
                "Proxy Authentication Required",
                "Request Timeout",
                "Conflict",
                "Gone",
                "Length Required",
                "Precondition Failed",
                "Payload Too Large",
                "Request-URI Too Long",
                "Unsupported Media Type",
                "Requested Range Not Satisfiable",
                "Expectation Failed",
                "I'm a teapot", null, null,
                "Misdirected Request",
                "Unprocessable Entity",
                "Locked",
                "Failed Dependency", null,
                "Upgrade Required", null,
                "Precondition Required",
                "Too Many Requests", null,
                "Request Header Fields Too Large",
                null, null, null, null, null, null, null, null, null, null, null, null,
                "Connection Closed Without Response",
                null, null, null, null, null, null,
                "Unavailable For Legal Reasons",
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                "Client Closed Request",
                "Internal Server Error",
                "Not Implemented",
                "Bad Gateway",
                "Service Unavailable",
                "Gateway Timeout",
                "HTTP Version Not Supported",
                "Variant Also Negotiates",
                "Insufficient Storage",
                "Loop Detected", null,
                "Not Extended",
                "Network Authentication Required"
        };

        static String get(int code) {
            return RESPONSE_CODES[code - 100];
        }
    }
}
