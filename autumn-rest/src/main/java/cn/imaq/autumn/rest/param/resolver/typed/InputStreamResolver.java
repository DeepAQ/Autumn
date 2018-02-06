package cn.imaq.autumn.rest.param.resolver.typed;

import cn.imaq.autumn.rest.param.resolver.TypedParamResolver;
import cn.imaq.autumn.rest.param.value.ParamValue;
import cn.imaq.autumn.rest.param.value.SingleValue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Parameter;

public class InputStreamResolver extends TypedParamResolver<InputStream> {
    @Override
    public ParamValue resolve(Parameter param, HttpServletRequest request, HttpServletResponse response) {
        try {
            return new SingleValue<>(request.getInputStream());
        } catch (IOException e) {
            return new SingleValue<>(null);
        }
    }
}
