package cn.imaq.tompuss.http;

import lombok.Getter;
import lombok.Setter;

import javax.servlet.http.Part;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TPFormPart implements Part {
    private byte[] dataSrc;

    @Getter
    @Setter
    private int offset;

    @Setter
    private int length;

    @Getter
    private Map<String, List<String>> headers = new HashMap<>();

    public TPFormPart(byte[] dataSrc) {
        this.dataSrc = dataSrc;
    }

    private String getHeaderParam(String header, String param) {
        if (!headers.containsKey(header)) {
            return null;
        }
        String[] dispositions = headers.get(header).get(0).split(";");
        for (int i = 1; i < dispositions.length; i++) {
            String[] nameAndValue = dispositions[i].trim().split("=", 2);
            if (nameAndValue.length == 2 && nameAndValue[0].equals(param)) {
                String value = nameAndValue[1];
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                return value;
            }
        }
        return null;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(dataSrc, offset, length);
    }

    @Override
    public String getContentType() {
        if (!headers.containsKey("content-type")) {
            return "text/plain";
        }
        return headers.get("content-type").get(0).split(";", 2)[0];
    }

    @Override
    public String getName() {
        return getHeaderParam("content-disposition", "name");
    }

    @Override
    public String getSubmittedFileName() {
        return getHeaderParam("content-disposition", "filename");
    }

    @Override
    public long getSize() {
        return length;
    }

    @Override
    public void write(String fileName) throws IOException {
        // TODO write file
    }

    @Override
    public void delete() throws IOException {
    }

    @Override
    public String getHeader(String name) {
        Collection<String> values = getHeaders(name);
        if (values == null) {
            return null;
        }
        return values.iterator().next();
    }

    @Override
    public Collection<String> getHeaders(String name) {
        String lower = name.toLowerCase();
        if (!headers.containsKey(lower)) {
            return null;
        }
        return headers.get(lower);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return headers.keySet();
    }
}
