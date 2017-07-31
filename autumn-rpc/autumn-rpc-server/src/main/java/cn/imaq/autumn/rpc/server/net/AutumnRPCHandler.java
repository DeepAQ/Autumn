package cn.imaq.autumn.rpc.server.net;

import cn.imaq.autumn.rpc.exception.AutumnInvokeException;
import cn.imaq.autumn.rpc.exception.AutumnSerializationException;
import cn.imaq.autumn.rpc.net.AutumnRPCRequest;
import cn.imaq.autumn.rpc.net.AutumnRPCResponse;
import cn.imaq.autumn.rpc.serialization.AutumnSerialization;
import cn.imaq.autumn.rpc.serialization.AutumnSerializationFactory;
import cn.imaq.autumn.rpc.server.invoker.AutumnInvoker;
import cn.imaq.autumn.rpc.server.invoker.AutumnInvokerFactory;
import cn.imaq.autumn.rpc.server.invoker.AutumnMethod;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import static cn.imaq.autumn.rpc.net.AutumnRPCResponse.STATUS_EXCEPTION;
import static cn.imaq.autumn.rpc.net.AutumnRPCResponse.STATUS_OK;

@Slf4j
public class AutumnRPCHandler implements AutumnHttpHandler {
    private final byte[] INFO_400 = "<html><head><title>400 Bad Request</title></head><body><center><h1>400 Bad Request</h1></center><hr><center>AutumnRPC</center></body></html>".getBytes();
    private final byte[] INFO_500 = "<html><head><title>500 Internal Server Error</title></head><body><center><h1>500 Internal Server Error</h1></center><hr><center>AutumnRPC</center></body></html>".getBytes();

    private final InstanceMap instanceMap = new InstanceMap();
    private final Properties config;
    private final AutumnInvoker invoker;
    private final AutumnSerialization serialization;

    public AutumnRPCHandler(Properties config) {
        this.config = config;
        // init
        this.invoker = AutumnInvokerFactory.getInvoker(config.getProperty("autumn.invoker"));
        log.info("Using invoker: " + this.invoker.getClass().getSimpleName());
        this.serialization = AutumnSerializationFactory.getSerialization(config.getProperty("autumn.serialization"));
        log.info("Using serialization: " + this.serialization.getClass().getSimpleName());
    }

    public InstanceMap getInstanceMap() {
        return instanceMap;
    }

    @Override
    public AutumnHttpResponse handle(AutumnHttpRequest request) {
        log.debug("Received HTTP request: " + request.getMethod() + " " + request.getPath());
        if (request.getPath().equals("/")) {
            try {
                return ok(false, new ObjectMapper().writeValueAsBytes(config));
            } catch (JsonProcessingException e) {
                log.error("Error exporting config");
                return error();
            }
        }
        String[] paths = request.getPath().split("/");
        if (paths.length >= 2) {
            String serviceName = paths[1];
            Object instance = instanceMap.getInstance(serviceName);
            if (instance != null) {
                // parse request
                byte[] body = request.getBody();
                try {
                    AutumnRPCRequest rpcRequest = serialization.deserializeRequest(body);
                    try {
                        Object result = invoker.invoke(instance, new AutumnMethod(instance.getClass(), rpcRequest.getMethodName(), rpcRequest.getParamTypes()), rpcRequest.getParams());
                        return ok(true, serialization.serializeResponse(
                                AutumnRPCResponse.builder().status(STATUS_OK).result(result).resultType(result.getClass()).build()
                        ));
                    } catch (AutumnInvokeException e) {
                        log.error("Error invoking " + serviceName + "#" + rpcRequest.getMethodName() + ": " + e.getCause());
                        return error();
                    } catch (InvocationTargetException e) {
                        log.error(serviceName + "#" + rpcRequest.getMethodName() + " threw an exception: " + e.getCause());
                        return ok(true, serialization.serializeResponse(
                                AutumnRPCResponse.builder().status(STATUS_EXCEPTION).result(e.getCause()).resultType(e.getCause().getClass()).build()
                        ));
                    }
                } catch (AutumnSerializationException e) {
                    log.error("Error parsing request: " + e.toString());
                }
            }
        }
        return badRequest();
    }

    private AutumnHttpResponse ok(boolean keepAlive, byte[] body) {
        return AutumnHttpResponse.builder()
                .code(200)
                .keepAlive(keepAlive)
                .contentType("application/octet-stream")
                .body(body)
                .build();
    }

    private AutumnHttpResponse badRequest() {
        return AutumnHttpResponse.builder()
                .code(400)
                .keepAlive(false)
                .contentType("text/html")
                .body(INFO_400)
                .build();
    }

    private AutumnHttpResponse error() {
        return AutumnHttpResponse.builder()
                .code(400)
                .keepAlive(false)
                .contentType("text/html")
                .body(INFO_500)
                .build();
    }
}
