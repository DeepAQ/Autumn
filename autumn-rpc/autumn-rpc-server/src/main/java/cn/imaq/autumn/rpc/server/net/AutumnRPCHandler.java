package cn.imaq.autumn.rpc.server.net;

import cn.imaq.autumn.core.context.AutumnContext;
import cn.imaq.autumn.rpc.exception.RPCInvokeException;
import cn.imaq.autumn.rpc.exception.RPCSerializationException;
import cn.imaq.autumn.rpc.net.AutumnRPCRequest;
import cn.imaq.autumn.rpc.net.AutumnRPCResponse;
import cn.imaq.autumn.rpc.serialization.AutumnSerializationFactory;
import cn.imaq.autumn.rpc.serialization.RPCSerialization;
import cn.imaq.autumn.rpc.server.context.RPCContext;
import cn.imaq.autumn.rpc.server.invoke.AutumnInvokerFactory;
import cn.imaq.autumn.rpc.server.invoke.RPCMethod;
import cn.imaq.autumn.rpc.server.invoke.RPCMethodInvoker;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import static cn.imaq.autumn.rpc.net.AutumnRPCResponse.STATUS_EXCEPTION;
import static cn.imaq.autumn.rpc.net.AutumnRPCResponse.STATUS_OK;

@Slf4j
public class AutumnRPCHandler implements RPCHttpHandler {
    private final byte[] INFO_400 = "<html><head><title>400 Bad Request</title></head><body><center><h1>400 Bad Request</h1></center><hr><center>AutumnRPC</center></body></html>".getBytes();
    private final byte[] INFO_500 = "<html><head><title>500 Internal Server Error</title></head><body><center><h1>500 Internal Server Error</h1></center><hr><center>AutumnRPC</center></body></html>".getBytes();

    private final RPCContext rpcContext;
    private final AutumnContext context;
    private final Properties config;
    private final RPCMethodInvoker invoker;
    private final RPCSerialization serialization;

    public AutumnRPCHandler(Properties config, AutumnContext context) {
        this.config = config;
        this.context = context;
        this.rpcContext = RPCContext.getFrom(context);
        // init
        this.invoker = AutumnInvokerFactory.getInvoker(config.getProperty("autumn.invoker"));
        log.info("Using invoker: {}", this.invoker.getClass().getSimpleName());
        this.serialization = AutumnSerializationFactory.getSerialization(config.getProperty("autumn.serialization"));
        log.info("Using serialization {}", this.serialization.getClass().getSimpleName());
    }

    @Override
    public RPCHttpResponse handle(RPCHttpRequest request) {
        log.debug("Received HTTP request: {} {}", request.getMethod(), request.getPath());
        if (request.getPath().equals("/")) {
            // TODO new config auto negotiation protocol
        }

        String[] paths = request.getPath().split("/");
        if (paths.length >= 2) {
            String serviceName = paths[1];
            Object instance = context.getBeanByType(rpcContext.findServiceClass(serviceName));
            if (instance != null) {
                byte[] body = request.getBody();
                try {
                    AutumnRPCRequest rpcRequest = serialization.deserializeRequest(body);
                    try {
                        Object result = invoker.invoke(instance,
                                new RPCMethod(instance.getClass(), rpcRequest.getMethodName(), rpcRequest.getParams().length, rpcRequest.getParamTypes()),
                                rpcRequest.getParams(), serialization
                        );
                        return ok(serialization.contentType(), serialization.serializeResponse(
                                AutumnRPCResponse.builder().status(STATUS_OK).result(result).resultType(result == null ? null : result.getClass()).build()
                        ));
                    } catch (RPCInvokeException e) {
                        log.error("Error invoking {}#{}: {}", serviceName, rpcRequest.getMethodName(), String.valueOf(e.getCause()));
                        return error();
                    } catch (InvocationTargetException e) {
                        log.info("{}#{} threw an exception: {}", serviceName, rpcRequest.getMethodName(), e.getCause());
                        return ok(serialization.contentType(), serialization.serializeResponse(
                                AutumnRPCResponse.builder().status(STATUS_EXCEPTION).result(e.getCause()).resultType(e.getCause().getClass()).build()
                        ));
                    }
                } catch (RPCSerializationException e) {
                    log.error("Error parsing request: {}", e.toString());
                }
            }
        }
        return badRequest();
    }

    private RPCHttpResponse ok(String contentType, byte[] body) {
        return RPCHttpResponse.builder()
                .code(200)
                .contentType(contentType)
                .body(body)
                .build();
    }

    private RPCHttpResponse badRequest() {
        return RPCHttpResponse.builder()
                .code(400)
                .contentType("text/html")
                .body(INFO_400)
                .build();
    }

    private RPCHttpResponse error() {
        return RPCHttpResponse.builder()
                .code(400)
                .contentType("text/html")
                .body(INFO_500)
                .build();
    }
}
