package cn.imaq.autumn.rest.param.resolver.typed;

import cn.imaq.autumn.rest.param.resolver.TypedParamResolver;
import cn.imaq.autumn.rest.param.value.ParamValue;
import cn.imaq.autumn.rest.param.value.SingleValue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Parameter;

public class ServletResponseResolver extends TypedParamResolver<HttpServletResponse> {
    @Override
    public ParamValue resolve(Parameter param, HttpServletRequest request, HttpServletResponse response) {
        return new SingleValue<>(response);
    }
}
