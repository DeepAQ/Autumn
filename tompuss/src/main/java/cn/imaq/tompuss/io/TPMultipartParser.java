package cn.imaq.tompuss.io;

import cn.imaq.autumn.http.protocol.AutumnByteArrayReader;
import cn.imaq.autumn.http.protocol.AutumnHttpRequest;
import cn.imaq.tompuss.servlet.TPFormPart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TPMultipartParser {
    public static Map<String, TPFormPart> parse(AutumnHttpRequest httpRequest) {
        // Check Content-Type
        if (!httpRequest.getHeaders().containsKey("content-type") || httpRequest.getBody() == null) {
            return Collections.emptyMap();
        }
        String[] contentTypes = httpRequest.getHeaders().get("content-type").get(0).split(";");
        if (!contentTypes[0].trim().equals("multipart/form-data")) {
            return Collections.emptyMap();
        }
        String boundary = null;
        for (int i = 1; i < contentTypes.length; i++) {
            String[] nameAndValue = contentTypes[i].trim().split("=", 2);
            if (nameAndValue.length == 2 && nameAndValue[0].equals("boundary")) {
                String value = nameAndValue[1];
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                boundary = "--" + value;
                break;
            }
        }
        if (boundary == null) {
            return Collections.emptyMap();
        }

        // Parse
        AutumnByteArrayReader reader = new AutumnByteArrayReader(httpRequest.getBody());
        Map<String, TPFormPart> partMap = new HashMap<>();
        State state = State.START;
        String line;
        TPFormPart part = null;
        int bodyStart = 0;
        int readBytes = 0;
        while ((line = reader.nextLine()) != null) {
            readBytes += line.getBytes().length + 2;
            if (state == State.START && line.equals(boundary)) {
                state = State.HEADER;
                part = new TPFormPart(httpRequest.getBody());
            } else if (state == State.HEADER) {
                if (line.isEmpty()) {
                    state = State.BODY;
                    bodyStart = readBytes;
                } else {
                    String[] kv = line.split(":", 2);
                    if (kv.length == 2) {
                        part.getHeaders().computeIfAbsent(kv[0].trim().toLowerCase(), k -> new ArrayList<>()).add(kv[1].trim());
                    } else {
                        state = State.START;
                    }
                }
            } else if (state == State.BODY) {
                if (line.equals(boundary) || line.equals(boundary + "--")) {
                    part.setOffset(bodyStart);
                    part.setLength(readBytes - line.getBytes().length - 2 - bodyStart);
                    partMap.put(part.getName(), part);
                    part = new TPFormPart(httpRequest.getBody());
                    state = State.HEADER;
                }
            }
        }
        if (state == State.BODY && part.getOffset() <= 0) {
            part.setOffset(bodyStart);
            part.setLength(readBytes - 2 - bodyStart);
            partMap.put(part.getName(), part);
        }
        return partMap;
    }

    private enum State {
        START, HEADER, BODY;
    }
}
