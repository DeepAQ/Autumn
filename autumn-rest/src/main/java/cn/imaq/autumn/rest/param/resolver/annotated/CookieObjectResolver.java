package cn.imaq.autumn.rest.param.resolver.annotated;

import cn.imaq.autumn.rest.annotation.param.CookieObject;
import cn.imaq.autumn.rest.param.resolver.AnnotatedParamResolver;
import cn.imaq.autumn.rest.param.value.MultipleValue;
import cn.imaq.autumn.rest.param.value.ParamValue;
import cn.imaq.autumn.rest.param.value.SingleValue;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Parameter;

public class CookieObjectResolver extends AnnotatedParamResolver<CookieObject> {
    @Override
    protected ParamValue resolve(Parameter param, CookieObject anno, HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (anno.value().isEmpty()) {
            return new MultipleValue<>(cookies);
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(anno.value())) {
                return new SingleValue<>(cookie);
            }
        }
        return new SingleValue<>(null);
    }
}
