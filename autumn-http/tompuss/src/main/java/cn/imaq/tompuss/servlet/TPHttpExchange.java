package cn.imaq.tompuss.servlet;

import lombok.Data;

import javax.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.List;

@Data
public class TPHttpExchange {
    private List<Cookie> cookies = new ArrayList<>();
}
