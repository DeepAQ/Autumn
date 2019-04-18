package cn.imaq.autumn.rpc.serialization;

import cn.imaq.autumn.rpc.exception.RPCSerializationException;
import cn.imaq.autumn.rpc.net.AutumnRPCRequest;
import cn.imaq.autumn.rpc.net.AutumnRPCResponse;

import java.lang.reflect.Type;

public interface RPCSerialization {
    String contentType();

    byte[] serializeRequest(AutumnRPCRequest request) throws RPCSerializationException;

    AutumnRPCRequest deserializeRequest(byte[] buf) throws RPCSerializationException;

    byte[] serializeResponse(AutumnRPCResponse response) throws RPCSerializationException;

    AutumnRPCResponse deserializeResponse(byte[] buf, Class<?> defaultReturnType) throws RPCSerializationException;

    Object[] convertTypes(Object[] src, Type[] types) throws RPCSerializationException;
}
