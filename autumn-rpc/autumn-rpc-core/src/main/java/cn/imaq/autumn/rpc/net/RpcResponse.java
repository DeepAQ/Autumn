package cn.imaq.autumn.rpc.net;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class RpcResponse implements Serializable {
    public static final int STATUS_OK = 0;
    public static final int STATUS_EXCEPTION = -1;

    private int status;

    private Object result;

    private Class<?> resultType;
}
