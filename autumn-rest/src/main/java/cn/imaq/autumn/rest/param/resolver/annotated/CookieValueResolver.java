package cn.imaq.autumn.rest.param.resolver.annotated;

import cn.imaq.autumn.rest.annotation.param.CookieValue;
import cn.imaq.autumn.rest.param.resolver.AnnotatedParamResolver;
import cn.imaq.autumn.rest.param.value.ParamValue;
import cn.imaq.autumn.rest.param.value.SingleValue;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Parameter;

public class CookieValueResolver extends AnnotatedParamResolver<CookieValue> {
    @Override
    protected ParamValue resolve(Parameter param, CookieValue anno, HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(anno.value())) {
                    return new SingleValue<>(cookie.getValue());
                }
            }
        }
        if (anno.required()) {
            return new SingleValue<>(anno.defaultValue());
        } else {
            return new SingleValue<>(null);
        }
    }
}
