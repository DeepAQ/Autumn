package cn.imaq.autumn.rpc.server.net;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AutumnRPCResponse {
    private int status;

    private Object result;
}
