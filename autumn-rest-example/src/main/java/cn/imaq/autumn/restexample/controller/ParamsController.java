package cn.imaq.autumn.restexample.controller;

import cn.imaq.autumn.rest.annotation.RequestMapping;
import cn.imaq.autumn.rest.annotation.RestController;
import cn.imaq.autumn.rest.annotation.param.*;

import javax.servlet.http.Cookie;
import java.util.List;

@RestController
@RequestMapping("/params")
public class ParamsController {
    @RequestMapping("/cookies")
    public Object cookies(@CookieObject List<Cookie> cookies) {
        return cookies;
    }

    @RequestMapping("/cookieValue")
    public Object cookieValue(@CookieValue("test1") String cookie1, @CookieValue("test2") String cookie2) {
        return cookie1 + "," + cookie2;
    }

    @RequestMapping("/body")
    public Object body(@RequestBody String body) {
        return body;
    }

    @RequestMapping("/header")
    public Object header(@RequestHeader("test1") String header1, @RequestHeader("test2") String header2) {
        return header1 + "," + header2;
    }

    @RequestMapping("/params")
    public Object params(@RequestParam("test1") String param1, @RequestParam("test2") String param2) {
        return param1 + "," + param2;
    }
}
