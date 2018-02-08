package cn.imaq.autumn.rest.message;

import cn.imaq.autumn.rest.exception.MessageConvertException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Type;

public class JacksonMessageConverter implements MessageConverter {
    private ObjectMapper jsonMapper = new ObjectMapper();

    @Override
    public String getContentType() {
        return "application/json";
    }

    @Override
    public byte[] toBytes(Object src) throws MessageConvertException {
        try {
            return jsonMapper.writeValueAsBytes(src);
        } catch (JsonProcessingException e) {
            throw new MessageConvertException(e);
        }
    }

    @Override
    public Object fromBytes(byte[] src, Type type) throws MessageConvertException {
        try {
            return jsonMapper.readValue(src, jsonMapper.constructType(type));
        } catch (IOException e) {
            throw new MessageConvertException(e);
        }
    }
}
