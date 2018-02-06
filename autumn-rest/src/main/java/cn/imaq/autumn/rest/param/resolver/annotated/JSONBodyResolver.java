package cn.imaq.autumn.rest.param.resolver.annotated;

import cn.imaq.autumn.rest.annotation.param.JSONBody;
import cn.imaq.autumn.rest.param.resolver.AnnotatedParamResolver;
import cn.imaq.autumn.rest.param.value.ParamValue;
import cn.imaq.autumn.rest.param.value.SingleValue;
import cn.imaq.autumn.rest.util.IOUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Parameter;

public class JSONBodyResolver extends AnnotatedParamResolver<JSONBody> {
    private ObjectMapper jsonMapper = new ObjectMapper();

    @Override
    protected ParamValue resolve(Parameter param, JSONBody anno, HttpServletRequest request, HttpServletResponse response) {
        try {
            byte[] bytes = IOUtil.readInputStream(request.getInputStream());
            JsonNode root = jsonMapper.readTree(bytes);
            if (anno.value().isEmpty()) {
                return new SingleValue<>(root);
            } else {
                return new SingleValue<>(root.get(anno.value()));
            }
        } catch (IOException e) {
            return new SingleValue<>(null);
        }
    }
}
