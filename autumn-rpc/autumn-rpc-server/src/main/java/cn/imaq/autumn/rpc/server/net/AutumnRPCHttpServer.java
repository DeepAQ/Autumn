package cn.imaq.autumn.rpc.server.net;

import cn.imaq.autumn.rpc.server.util.InstanceMap;
import cn.imaq.autumn.rpc.server.util.LogUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.rapidoid.buffer.Buf;
import org.rapidoid.http.AbstractHttpServer;
import org.rapidoid.http.HttpResponseCodes;
import org.rapidoid.http.HttpStatus;
import org.rapidoid.http.MediaType;
import org.rapidoid.net.abstracts.Channel;
import org.rapidoid.net.impl.RapidoidHelper;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AutumnRPCHttpServer extends AbstractHttpServer {
    private final byte[] ROOT_PATH = new byte[]{'/'};
    private final byte[] METHOD_POST = "POST".getBytes();
    private final byte[] INFO_400 = "<html><head><title>400 Bad Request</title></head><body><center><h1>400 Bad Request</h1></center><hr><center>AutumnRPC</center></body></html>".getBytes();
    private final byte[] INFO_500 = "<html><head><title>500 Internal Server Error</title></head><body><center><h1>500 Internal Server Error</h1></center><hr><center>AutumnRPC</center></body></html>".getBytes();

    private final InstanceMap instanceMap = new InstanceMap();

    public AutumnRPCHttpServer() {
        super("AutumnRPC", "Not Found", "Internal Server Error", true);
    }

    public InstanceMap getInstanceMap() {
        return instanceMap;
    }

    @Override
    protected HttpStatus handle(Channel ctx, Buf buf, RapidoidHelper req) {
        String verb = req.verb.str(buf);
        String path = req.path.str(buf);
        LogUtil.D("Received HTTP request: " + verb + " " + path);
        if (req.isGet.value) {
            if (matches(buf, req.path, ROOT_PATH)) {
                // TODO
            }
        }
        String[] paths = path.split("/");
        if (paths.length >= 2) {
            String serviceName = paths[1];
            Object instance = instanceMap.getInstance(serviceName);
            if (instance != null) {
                // parse request
                byte[] body = req.body.bytes(buf);
                ObjectMapper mapper = new ObjectMapper();
                try {
                    AutumnRPCRequest request = mapper.readValue(body, AutumnRPCRequest.class);
                    if (request.getParamTypes().length == request.getParams().length) {
                        try {
                            Method method = instance.getClass().getMethod(request.getMethodName(), request.getParamTypes());
                            Object[] realParams = new Object[request.getParams().length];
                            for (int i = 0; i < request.getParams().length; i++) {
                                realParams[i] = mapper.treeToValue(request.getParams()[i], request.getParamTypes()[i]);
                            }
                            Object result = method.invoke(instance, realParams);
                            return ok(ctx, true, mapper.writeValueAsBytes(new AutumnRPCResponse(0, result)), MediaType.JSON);
                        } catch (NoSuchMethodException e) {
                            LogUtil.E("Method not found: " + serviceName + "#" + request.getMethodName());
                        } catch (IllegalAccessException e) {
                            LogUtil.E("Error invoking " + serviceName + "#" + request.getMethodName() + ": Illegal access");
                            return error(ctx);
                        } catch (InvocationTargetException e) {
                            LogUtil.E(serviceName + "#" + request.getMethodName() + " threw an exception: " + e.getCause());
                            return ok(ctx, true, mapper.writeValueAsBytes(new AutumnRPCResponse(-1, e.getCause())), MediaType.JSON);
                        }
                    }
                } catch (IOException e) {
                    LogUtil.E("Error parsing request: " + e.getClass().getSimpleName());
                }
            }
        }
        return badRequest(ctx);
    }

    private HttpStatus badRequest(Channel ctx) {
        ctx.write(HttpResponseCodes.get(400));
        writeCommonHeaders(ctx, false);
        writeBody(ctx, INFO_400, MediaType.HTML_UTF_8);
        return HttpStatus.DONE;
    }

    private HttpStatus error(Channel ctx) {
        ctx.write(HttpResponseCodes.get(500));
        writeCommonHeaders(ctx, false);
        writeBody(ctx, INFO_500, MediaType.HTML_UTF_8);
        return HttpStatus.DONE;
    }
}
