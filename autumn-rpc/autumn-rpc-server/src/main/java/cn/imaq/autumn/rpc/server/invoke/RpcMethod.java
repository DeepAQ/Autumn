package cn.imaq.autumn.rpc.server.invoke;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RpcMethod {
    private Class<?> type;

    private String name;

    private Integer paramCount;

    private Class<?>[] paramTypes;
}
