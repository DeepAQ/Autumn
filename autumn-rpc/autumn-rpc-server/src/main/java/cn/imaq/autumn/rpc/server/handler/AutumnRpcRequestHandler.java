package cn.imaq.autumn.rpc.server.handler;

import cn.imaq.autumn.core.context.AutumnContext;
import cn.imaq.autumn.rpc.exception.RpcSerializationException;
import cn.imaq.autumn.rpc.net.RpcRequest;
import cn.imaq.autumn.rpc.net.RpcResponse;
import cn.imaq.autumn.rpc.serialization.AutumnSerializationFactory;
import cn.imaq.autumn.rpc.serialization.RpcSerialization;
import cn.imaq.autumn.rpc.server.context.RpcContext;
import cn.imaq.autumn.rpc.server.exception.RpcInvocationException;
import cn.imaq.autumn.rpc.server.invoke.AutumnInvokerFactory;
import cn.imaq.autumn.rpc.server.invoke.RpcMethod;
import cn.imaq.autumn.rpc.server.invoke.RpcMethodInvoker;
import cn.imaq.autumn.rpc.server.net.RpcHttpRequest;
import cn.imaq.autumn.rpc.server.net.RpcHttpResponse;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import static cn.imaq.autumn.rpc.net.RpcResponse.STATUS_EXCEPTION;
import static cn.imaq.autumn.rpc.net.RpcResponse.STATUS_OK;

@Slf4j
public class AutumnRpcRequestHandler implements RpcRequestHandler {
    private final byte[] INFO_400 = "<html><head><title>400 Bad Request</title></head><body><center><h1>400 Bad Request</h1></center><hr><center>AutumnRPC</center></body></html>".getBytes();
    private final byte[] INFO_500 = "<html><head><title>500 Internal Server Error</title></head><body><center><h1>500 Internal Server Error</h1></center><hr><center>AutumnRPC</center></body></html>".getBytes();

    private final RpcContext rpcContext;
    private final AutumnContext context;
    private final Properties config;
    private final RpcMethodInvoker invoker;
    private final RpcSerialization serialization;

    public AutumnRpcRequestHandler(Properties config, AutumnContext context) {
        this.config = config;
        this.context = context;
        this.rpcContext = RpcContext.getFrom(context);
        // init
        this.invoker = AutumnInvokerFactory.getInvoker(config.getProperty("autumn.invoker"));
        log.info("Using invoker: {}", this.invoker.getClass().getSimpleName());
        this.serialization = AutumnSerializationFactory.getSerialization(config.getProperty("autumn.serialization"));
        log.info("Using serialization {}", this.serialization.getClass().getSimpleName());
    }

    @Override
    public RpcHttpResponse handle(RpcHttpRequest request) {
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
                    RpcRequest rpcRequest = serialization.deserializeRequest(body);
                    try {
                        Object result = invoker.invoke(instance,
                                new RpcMethod(instance.getClass(), rpcRequest.getMethodName(), rpcRequest.getParams().length, rpcRequest.getParamTypes()),
                                rpcRequest.getParams(), serialization
                        );
                        return ok(serialization.contentType(), serialization.serializeResponse(
                                RpcResponse.builder().status(STATUS_OK).result(result).resultType(result == null ? null : result.getClass()).build()
                        ));
                    } catch (RpcInvocationException e) {
                        log.error("Error invoking {}#{}: {}", serviceName, rpcRequest.getMethodName(), String.valueOf(e.getCause()));
                        return error();
                    } catch (InvocationTargetException e) {
//                        log.info("{}#{} threw an exception: {}", serviceName, rpcRequest.getMethodName(), e.getCause());
                        return ok(serialization.contentType(), serialization.serializeResponse(
                                RpcResponse.builder().status(STATUS_EXCEPTION).result(e.getCause()).resultType(e.getCause().getClass()).build()
                        ));
                    }
                } catch (RpcSerializationException e) {
                    log.error("Error parsing request: {}", e.toString());
                }
            }
        }
        return badRequest();
    }

    private RpcHttpResponse ok(String contentType, byte[] body) {
        return RpcHttpResponse.builder()
                .code(200)
                .contentType(contentType)
                .body(body)
                .build();
    }

    private RpcHttpResponse badRequest() {
        return RpcHttpResponse.builder()
                .code(400)
                .contentType("text/html")
                .body(INFO_400)
                .build();
    }

    private RpcHttpResponse error() {
        return RpcHttpResponse.builder()
                .code(400)
                .contentType("text/html")
                .body(INFO_500)
                .build();
    }
}
