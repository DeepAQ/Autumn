package cn.imaq.autumn.rest.param.resolver.annotated;

import cn.imaq.autumn.rest.annotation.param.SessionAttribute;
import cn.imaq.autumn.rest.param.resolver.AnnotatedParamResolver;
import cn.imaq.autumn.rest.param.value.ParamValue;
import cn.imaq.autumn.rest.param.value.SingleValue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Parameter;

public class SessionAttributeResolver extends AnnotatedParamResolver<SessionAttribute> {
    @Override
    protected ParamValue resolve(Parameter param, SessionAttribute anno, HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        Object attr = session.getAttribute(anno.value());
        if (attr == null) {
            if (anno.required()) {
                return new SingleValue<>(anno.defaultValue());
            } else {
                return new SingleValue<>(null);
            }
        }
        return new SingleValue<>(attr);
    }
}
