package cn.imaq.autumn.rpc.server.net;

import cn.imaq.autumn.rpc.server.util.ClassMap;
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

public class AutumnHttpServer extends AbstractHttpServer {
    private final byte[] ROOT_PATH = new byte[]{'/'};
    private final byte[] METHOD_POST = "POST".getBytes();
    private final byte[] INFO_400 = "<html><head><title>400 Bad Request</title></head><body><center><h1>400 Bad Request</h1></center><hr><center>AutumnRPC</center></body></html>".getBytes();

    private ClassMap classMap;

    public AutumnHttpServer() {
        super("AutumnRPC", "Not Found", "Internal Server Error", true);
        classMap = new ClassMap();
    }

    public ClassMap getClassMap() {
        return classMap;
    }

    @Override
    protected HttpStatus handle(Channel ctx, Buf buf, RapidoidHelper req) {
        String verb = req.verb.str(buf);
        String path = req.path.str(buf);
        LogUtil.D("Received HTTP Request:\n" + verb + " " + path);
        if (req.isGet.value) {
            if (matches(buf, req.path, ROOT_PATH)) {
                // TODO
            }
        }
        String[] paths = path.split("/");
        if (paths.length >= 2) {
            String className = paths[1];
            Class clz = classMap.getClass(className);
            if (clz != null) {
                // parse request
                byte[] body = req.body.bytes(buf);
                ObjectMapper mapper = new ObjectMapper();
                try {
                    AutumnRPCRequest request = mapper.readValue(body, AutumnRPCRequest.class);
                    if (request.getParamTypes().length != request.getParams().length) {
                        return badRequest(ctx);
                    }
                    try {
                        Method method = clz.getMethod(request.getMethodName(), request.getParamTypes());
                        Object instance = clz.newInstance();
                        Object result = method.invoke(instance, request.getParams());
                        return ok(ctx, true, mapper.writeValueAsBytes(new AutumnRPCResponse(0, result)), MediaType.JSON);
                    } catch (NoSuchMethodException e) {
                        LogUtil.E("Method not found: " + className + "#" + request.getMethodName());
                    } catch (InstantiationException e) {
                        LogUtil.E("Cannot instantiate " + clz.getName() + ": no nullary constructor found");
                    } catch (IllegalAccessException | IllegalArgumentException e) {
                        LogUtil.E("Error invoking " + clz.getName() + "#" + request.getMethodName() + ": " + e.getClass().getSimpleName() + " " + e.getLocalizedMessage());
                    } catch (InvocationTargetException e) {
                        LogUtil.E(clz.getName() + "#" + request.getMethodName() + " threw an exception: " + e.getCause());
                        return ok(ctx, true, mapper.writeValueAsBytes(new AutumnRPCResponse(-1, e.getCause())), MediaType.JSON);
                    }
                } catch (IOException e) {
                    LogUtil.E("Cannot parse request");
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
}
