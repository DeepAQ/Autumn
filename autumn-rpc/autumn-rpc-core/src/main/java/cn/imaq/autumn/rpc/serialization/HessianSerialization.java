package cn.imaq.autumn.rpc.serialization;

import cn.imaq.autumn.rpc.exception.RpcSerializationException;
import cn.imaq.autumn.rpc.net.RpcRequest;
import cn.imaq.autumn.rpc.net.RpcResponse;
import com.alibaba.com.caucho.hessian.io.Hessian2Input;
import com.alibaba.com.caucho.hessian.io.Hessian2Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;

public class HessianSerialization implements RpcSerialization {
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
    public byte[] serializeRequest(RpcRequest request) throws RpcSerializationException {
        try {
            return serializeObject(request);
        } catch (IOException e) {
            throw new RpcSerializationException(e);
        }
    }

    @Override
    public RpcRequest deserializeRequest(byte[] buf) throws RpcSerializationException {
        try {
            return (RpcRequest) deserializeObject(buf);
        } catch (IOException | ClassCastException e) {
            throw new RpcSerializationException(e);
        }
    }

    @Override
    public byte[] serializeResponse(RpcResponse response) throws RpcSerializationException {
        try {
            return serializeObject(response);
        } catch (IOException e) {
            throw new RpcSerializationException(e);
        }
    }

    @Override
    public RpcResponse deserializeResponse(byte[] buf, Class<?> defaultReturnType) throws RpcSerializationException {
        try {
            return (RpcResponse) deserializeObject(buf);
        } catch (IOException | ClassCastException e) {
            throw new RpcSerializationException(e);
        }
    }

    @Override
    public Object[] convertTypes(Object[] src, Type[] types) throws RpcSerializationException {
        return src;
    }
}
