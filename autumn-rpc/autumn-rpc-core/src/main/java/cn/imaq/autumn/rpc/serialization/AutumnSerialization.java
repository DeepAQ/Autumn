package cn.imaq.autumn.rpc.serialization;

import cn.imaq.autumn.rpc.exception.AutumnSerializationException;
import cn.imaq.autumn.rpc.net.AutumnRPCRequest;
import cn.imaq.autumn.rpc.net.AutumnRPCResponse;

public interface AutumnSerialization {
    byte[] serializeRequest(AutumnRPCRequest request) throws AutumnSerializationException;

    AutumnRPCRequest deserializeRequest(byte[] buf) throws AutumnSerializationException;

    byte[] serializeResponse(AutumnRPCResponse response) throws AutumnSerializationException;

    AutumnRPCResponse deserializeResponse(byte[] buf, Class defaultReturnType) throws AutumnSerializationException;
}
