package cn.imaq.autumn.rpc.serialization;

import cn.imaq.autumn.rpc.exception.RpcSerializationException;
import cn.imaq.autumn.rpc.net.RpcRequest;
import cn.imaq.autumn.rpc.net.RpcResponse;

import java.lang.reflect.Type;

public interface RpcSerialization {
    String contentType();

    byte[] serializeRequest(RpcRequest request) throws RpcSerializationException;

    RpcRequest deserializeRequest(byte[] buf) throws RpcSerializationException;

    byte[] serializeResponse(RpcResponse response) throws RpcSerializationException;

    RpcResponse deserializeResponse(byte[] buf, Class<?> defaultReturnType) throws RpcSerializationException;

    Object[] convertTypes(Object[] src, Type[] types) throws RpcSerializationException;
}
