package cn.imaq.autumn.rpc.serialization;

import cn.imaq.autumn.rpc.exception.AutumnSerializationException;
import cn.imaq.autumn.rpc.net.AutumnRPCRequest;
import cn.imaq.autumn.rpc.net.AutumnRPCResponse;
import com.alibaba.com.caucho.hessian.io.Hessian2Input;
import com.alibaba.com.caucho.hessian.io.Hessian2Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;

public class HessianSerialization implements AutumnSerialization {
    private byte[] serializeObject(Object object) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Hessian2Output hout = new Hessian2Output(os);
        hout.writeObject(object);
        hout.close();
        byte[] result = os.toByteArray();
        os.close();
        return result;
    }

    private Object deserializeObject(byte[] buf) throws IOException {
        ByteArrayInputStream is = new ByteArrayInputStream(buf);
        Hessian2Input hin = new Hessian2Input(is);
        Object object = hin.readObject();
        hin.close();
        is.close();
        return object;
    }

    @Override
    public String contentType() {
        return "application/octet-stream";
    }

    @Override
    public byte[] serializeRequest(AutumnRPCRequest request) throws AutumnSerializationException {
        try {
            return serializeObject(request);
        } catch (IOException e) {
            throw new AutumnSerializationException(e);
        }
    }

    @Override
    public AutumnRPCRequest deserializeRequest(byte[] buf) throws AutumnSerializationException {
        try {
            return (AutumnRPCRequest) deserializeObject(buf);
        } catch (IOException | ClassCastException e) {
            throw new AutumnSerializationException(e);
        }
    }

    @Override
    public byte[] serializeResponse(AutumnRPCResponse response) throws AutumnSerializationException {
        try {
            return serializeObject(response);
        } catch (IOException e) {
            throw new AutumnSerializationException(e);
        }
    }

    @Override
    public AutumnRPCResponse deserializeResponse(byte[] buf, Class defaultReturnType) throws AutumnSerializationException {
        try {
            return (AutumnRPCResponse) deserializeObject(buf);
        } catch (IOException | ClassCastException e) {
            throw new AutumnSerializationException(e);
        }
    }

    @Override
    public Object[] convertTypes(Object[] src, Type[] types) throws AutumnSerializationException {
        return src;
    }
}
