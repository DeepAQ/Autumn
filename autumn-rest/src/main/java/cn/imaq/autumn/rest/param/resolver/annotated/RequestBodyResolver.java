package cn.imaq.autumn.rest.param.resolver.annotated;

import cn.imaq.autumn.rest.annotation.param.RequestBody;
import cn.imaq.autumn.rest.param.resolver.AnnotatedParamResolver;
import cn.imaq.autumn.rest.param.value.ParamValue;
import cn.imaq.autumn.rest.param.value.SingleValue;
import cn.imaq.autumn.rest.util.IOUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Parameter;

public class RequestBodyResolver extends AnnotatedParamResolver<RequestBody> {
    @Override
    protected ParamValue resolve(Parameter param, RequestBody anno, HttpServletRequest request, HttpServletResponse response) {
        try {
            byte[] bytes = IOUtil.readInputStream(request.getInputStream());
            return new SingleValue<>(bytes);
        } catch (IOException e) {
            return new SingleValue<>(new byte[0]);
        }
    }
}
