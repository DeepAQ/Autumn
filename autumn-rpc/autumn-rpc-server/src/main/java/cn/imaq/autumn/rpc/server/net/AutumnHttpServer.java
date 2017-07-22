package cn.imaq.autumn.rpc.server.net;

import cn.imaq.autumn.rpc.server.util.ClassMap;
import cn.imaq.autumn.rpc.server.util.LogUtil;
import org.rapidoid.buffer.Buf;
import org.rapidoid.http.AbstractHttpServer;
import org.rapidoid.http.HttpResponseCodes;
import org.rapidoid.http.HttpStatus;
import org.rapidoid.http.MediaType;
import org.rapidoid.net.abstracts.Channel;
import org.rapidoid.net.impl.RapidoidHelper;

public class AutumnHttpServer extends AbstractHttpServer {
    private final byte[] ROOT_PATH = new byte[]{'/'};
    private final byte[] METHOD_POST = "POST".getBytes();
    private final byte[] INFO_403 = "<html><head><title>403 Forbidden</title></head><body><center><h1>403 Forbidden</h1></center><hr><center>AutumnRPC</center></body></html>".getBytes();

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
        String method = req.verb.str(buf);
        String path = req.path.str(buf);
        LogUtil.D("Received HTTP Request:\n" + method + " " + path);
        if (req.isGet.value) {
            if (matches(buf, req.path, ROOT_PATH)) {
                // TODO
            }
        } else if (matches(buf, req.verb, METHOD_POST)) {
            // TODO
        }
        return forbidden(ctx);
    }

    private HttpStatus forbidden(Channel ctx) {
        ctx.write(HttpResponseCodes.get(403));
        writeCommonHeaders(ctx, false);
        writeBody(ctx, INFO_403, MediaType.HTML_UTF_8);
        return HttpStatus.DONE;
    }
}
