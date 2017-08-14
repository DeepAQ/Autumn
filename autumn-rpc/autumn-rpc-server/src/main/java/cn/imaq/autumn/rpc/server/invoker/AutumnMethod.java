package cn.imaq.autumn.rpc.server.invoker;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AutumnMethod {
    private Class type;

    private String name;

    private Integer paramCount;

    private Class[] paramTypes;
}
