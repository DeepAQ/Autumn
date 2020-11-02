package cn.imaq.autumn.http.protocol;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class AutumnHttpResponse {
    private String protocol;

    private int status;

    private Map<String, List<String>> headers;

    private String contentType;

    private byte[] body;

    public byte[] toHeaderBytes() {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(status).append(' ').append(ResponseCodes.get(status)).append("\r\n");
        boolean sentContentType = false;
        boolean sentContentLength = false;
        if (headers != null) {
            for (Map.Entry<String, List<String>> header : headers.entrySet()) {
                String key = header.getKey();
                for (String value : header.getValue()) {
                    sb.append(key).append(": ").append(value).append("\r\n");
                }
                if (key.toLowerCase().equals("content-type")) {
                    sentContentType = true;
                } else if (key.toLowerCase().equals("content-length")) {
                    sentContentLength = true;
                }
            }
        }
        if (!sentContentType && contentType != null) {
            sb.append("Content-Type: ").append(contentType).append("\r\n");
        }
        if (!sentContentLength && body != null) {
            sb.append("Content-Length: ").append(body.length).append("\r\n");
        }
        sb.append("\r\n");
        return sb.toString().getBytes();
    }

    public static class ResponseCodes {
        static final String[][] RESPONSE_CODES = {
                {"Continue", "Switching Protocols", "Processing", "Early Hints"},
                {"OK", "Created", "Accepted", "Non-authoritative Information", "No Content", "Reset Content", "Partial Content", "Multi-Status", "Already Reported"},
                {"Multiple Choices", "Moved Permanently", "Found", "See Other", "Not Modified", "Use Proxy", "Switch Proxy", "Temporary Redirect", "Permanent Redirect"},
                {"Bad Request", "Unauthorized", "Payment Required", "Forbidden", "Not Found", "Method Not Allowed", "Not Acceptable", "Proxy Authentication Required", "Request Timeout", "Conflict", "Gone", "Length Required", "Precondition Failed", "Payload Too Large", "Request-URI Too Long", "Unsupported Media Type", "Requested Range Not Satisfiable", "Expectation Failed", "I'm a teapot", null, null, "Misdirected Request", "Unprocessable Entity", "Locked", "Failed Dependency", "Too Early", "Upgrade Required", null, "Precondition Required", "Too Many Requests", null, "Request Header Fields Too Large", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, "Unavailable For Legal Reasons"},
                {"Internal Server Error", "Not Implemented", "Bad Gateway", "Service Unavailable", "Gateway Timeout", "HTTP Version Not Supported", "Variant Also Negotiates", "Insufficient Storage", "Loop Detected", null, "Not Extended", "Network Authentication Required"}
        };

        public static String get(int code) {
            try {
                return RESPONSE_CODES[code / 100 - 1][code % 100];
            } catch (Exception e) {
                return null;
            }
        }
    }
}
