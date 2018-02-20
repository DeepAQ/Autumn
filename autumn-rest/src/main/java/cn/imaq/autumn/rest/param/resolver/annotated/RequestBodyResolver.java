package cn.imaq.autumn.rest.param.resolver.annotated;

import cn.imaq.autumn.rest.annotation.param.RequestBody;
import cn.imaq.autumn.rest.core.RestContext;
import cn.imaq.autumn.rest.message.MessageConverter;
import cn.imaq.autumn.rest.param.resolver.AnnotatedParamResolver;
import cn.imaq.autumn.rest.param.value.ParamValue;
import cn.imaq.autumn.rest.param.value.SingleValue;
import cn.imaq.autumn.rest.util.IOUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Parameter;

public class RequestBodyResolver extends AnnotatedParamResolver<RequestBody> {
    @Override
    protected ParamValue resolve(Parameter param, RequestBody anno, HttpServletRequest request, HttpServletResponse response) {
        try {
            byte[] bytes = IOUtil.readInputStream(request.getInputStream());
            Class<?> paramType = param.getType();
            if (paramType.isAssignableFrom(byte[].class) || paramType.isAssignableFrom(String.class)) {
                return new SingleValue<>(bytes);
            } else {
                RestContext restContext = (RestContext) request.getServletContext().getAttribute(RestContext.ATTR);
                if (restContext != null) {
                    MessageConverter converter = restContext.getInstance(anno.converter());
                    return new SingleValue<>(converter.fromBytes(bytes, param.getParameterizedType()));
                } else {
                    return new SingleValue<>(null);
                }
            }
        } catch (Exception e) {
            return new SingleValue<>(new byte[0]);
        }
    }
}
