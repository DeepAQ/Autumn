package cn.imaq.autumn.rpc.serialization;

import cn.imaq.autumn.rpc.exception.RpcSerializationException;
import cn.imaq.autumn.rpc.net.RpcRequest;
import cn.imaq.autumn.rpc.net.RpcResponse;
import cn.imaq.autumn.rpc.util.PrimitiveClassUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JsonSerialization implements RpcSerialization {
    private ObjectMapper mapper = new ObjectMapper();
    private Map<String, Class<?>> classMap = new ConcurrentHashMap<>();

    @Override
    public String contentType() {
        return "application/json";
    }

    @Override
    public byte[] serializeRequest(RpcRequest request) throws RpcSerializationException {
        ArrayNode result = mapper.createArrayNode();
        result.add(request.getMethodName());
        ArrayNode paramTypesNode = mapper.createArrayNode();
        if (request.getParamTypes() != null) {
            Arrays.stream(request.getParamTypes()).forEach(pt -> paramTypesNode.add(pt.getName()));
        }
        result.add(paramTypesNode);
        ArrayNode paramsNode = mapper.createArrayNode();
        if (request.getParams() != null) {
            Arrays.stream(request.getParams()).forEach(p -> paramsNode.add(mapper.valueToTree(p)));
        }
        result.add(paramsNode);
        try {
            return mapper.writeValueAsBytes(result);
        } catch (JsonProcessingException e) {
            throw new RpcSerializationException(e);
        }
    }

    @Override
    public RpcRequest deserializeRequest(byte[] buf) throws RpcSerializationException {
        try {
            JsonNode root = mapper.readTree(buf);
            if (root.isArray() && root.size() >= 3) {
                Class<?>[] paramTypes = null;
                if (root.get(1).isArray()) {
                    paramTypes = new Class<?>[root.get(1).size()];
                }
                JsonNode[] params = new JsonNode[root.get(2).size()];
                for (int i = 0; i < root.get(2).size(); i++) {
                    if (paramTypes != null && paramTypes.length > i) {
                        paramTypes[i] = getClass(root.get(1).get(i).textValue());
                    }
                    params[i] = root.get(2).get(i);
                }
                return RpcRequest.builder()
                        .methodName(root.get(0).textValue())
                        .paramTypes(paramTypes)
                        .params(params)
                        .build();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RpcSerializationException(e);
        }
        throw new RpcSerializationException("JSON format error");
    }

    @Override
    public byte[] serializeResponse(RpcResponse response) throws RpcSerializationException {
        ArrayNode result = mapper.createArrayNode();
        result.add(response.getStatus());
        result.add(mapper.valueToTree(response.getResult()));
        if (response.getResultType() != null) {
            result.add(response.getResultType().getName());
        }
        try {
            return mapper.writeValueAsBytes(result);
        } catch (JsonProcessingException e) {
            throw new RpcSerializationException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public RpcResponse deserializeResponse(byte[] buf, Class<?> defaultReturnType) throws RpcSerializationException {
        try {
            JsonNode root = mapper.readTree(buf);
            if (root.isArray() && root.size() >= 2) {
                Class<?> returnType = defaultReturnType;
                Object result = root.get(1);
                if (root.size() >= 3) {
                    try {
                        returnType = getClass(root.get(2).textValue());
                        result = mapper.treeToValue(((JsonNode) result), returnType);
                    } catch (ClassNotFoundException ignored) {
                    }
                }
                return RpcResponse.builder()
                        .status(root.get(0).intValue())
                        .result(result)
                        .resultType(returnType)
                        .build();
            }
        } catch (IOException e) {
            throw new RpcSerializationException(e);
        }
        throw new RpcSerializationException("JSON format error");
    }

    @Override
    public Object[] convertTypes(Object[] src, Type[] types) throws RpcSerializationException {
        try {
            Object[] result = new Object[src.length];
            for (int i = 0; i < src.length; i++) {
                result[i] = mapper.convertValue(src[i], mapper.constructType(types[i]));
            }
            return result;
        } catch (Exception e) {
            throw new RpcSerializationException(e);
        }
    }

    private Class<?> getClass(String className) throws ClassNotFoundException {
        Class<?> clazz = classMap.get(className);
        if (clazz == null) {
            clazz = PrimitiveClassUtil.get(className);
            if (clazz == null) {
                clazz = Class.forName(className);
            }
            classMap.put(className, clazz);
        }
        return clazz;
    }
}
