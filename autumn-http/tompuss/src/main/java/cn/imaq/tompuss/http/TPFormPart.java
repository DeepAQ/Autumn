package cn.imaq.tompuss.http;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class TPFormPart {
    private byte[] dataSrc;
    private int offset;
    private int length;
    private Map<String, List<String>> headers = new HashMap<>();

    public TPFormPart(byte[] dataSrc) {
        this.dataSrc = dataSrc;
    }
}
