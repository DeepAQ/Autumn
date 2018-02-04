package cn.imaq.autumn.rest.param.resolver.annotated;

import cn.imaq.autumn.rest.annotation.param.RequestParam;
import cn.imaq.autumn.rest.param.resolver.AnnotatedParamResolver;
import cn.imaq.autumn.rest.param.value.MultipleValue;
import cn.imaq.autumn.rest.param.value.ParamValue;
import cn.imaq.autumn.rest.param.value.SingleValue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Parameter;

public class RequestParamResolver extends AnnotatedParamResolver<RequestParam> {
    @Override
    protected ParamValue resolve(Parameter param, RequestParam anno, HttpServletRequest request, HttpServletResponse response) {
        if (anno.value().isEmpty()) {
            return new SingleValue<>(request.getParameterMap());
        }
        String[] values = request.getParameterValues(anno.value());
        if (values == null || values.length == 0) {
            if (anno.required()) {
                return new SingleValue<>(anno.defaultValue());
            } else {
                return new SingleValue<>(null);
            }
        }
        return new MultipleValue<>(values);
    }
}
