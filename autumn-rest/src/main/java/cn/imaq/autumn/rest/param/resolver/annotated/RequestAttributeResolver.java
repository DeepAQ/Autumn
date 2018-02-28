package cn.imaq.autumn.rest.param.resolver.annotated;

import cn.imaq.autumn.rest.annotation.param.SessionAttribute;
import cn.imaq.autumn.rest.param.resolver.AnnotatedParamResolver;
import cn.imaq.autumn.rest.param.value.ParamValue;
import cn.imaq.autumn.rest.param.value.SingleValue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Parameter;

public class RequestAttributeResolver extends AnnotatedParamResolver<SessionAttribute> {
    @Override
    protected ParamValue resolve(Parameter param, SessionAttribute anno, HttpServletRequest request, HttpServletResponse response) {
        Object attr = request.getAttribute(anno.value());
        if (attr == null && anno.required()) {
            return new SingleValue<>(anno.defaultValue());
        }
        return new SingleValue<>(attr);
    }
}
