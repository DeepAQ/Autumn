package cn.imaq.autumn.rpc.net;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class RpcRequest implements Serializable {
    private String methodName;

    private Class<?>[] paramTypes;

    private Object[] params;
}
