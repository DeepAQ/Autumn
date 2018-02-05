package cn.imaq.autumn.rest.param.resolver.annotated;

import cn.imaq.autumn.rest.annotation.param.RequestHeader;
import cn.imaq.autumn.rest.param.resolver.AnnotatedParamResolver;
import cn.imaq.autumn.rest.param.value.MultipleValue;
import cn.imaq.autumn.rest.param.value.ParamValue;
import cn.imaq.autumn.rest.param.value.SingleValue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.Enumeration;

public class RequestHeaderResolver extends AnnotatedParamResolver<RequestHeader> {
    @Override
    protected ParamValue resolve(Parameter param, RequestHeader anno, HttpServletRequest request, HttpServletResponse response) {
        Enumeration<String> headers = request.getHeaders(anno.value());
        if (headers == null || !headers.hasMoreElements()) {
            if (anno.required()) {
                return new SingleValue<>(anno.defaultValue());
            } else {
                return new SingleValue<>(null);
            }
        }
        return new MultipleValue<>(Collections.list(headers));
    }
}
