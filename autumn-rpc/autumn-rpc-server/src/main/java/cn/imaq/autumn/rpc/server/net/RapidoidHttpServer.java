package cn.imaq.autumn.rpc.server.net;

import cn.imaq.autumn.rpc.server.exception.AutumnHttpException;
import org.rapidoid.buffer.Buf;
import org.rapidoid.http.AbstractHttpServer;
import org.rapidoid.http.HttpResponseCodes;
import org.rapidoid.http.HttpStatus;
import org.rapidoid.http.impl.lowlevel.HttpIO;
import org.rapidoid.net.Server;
import org.rapidoid.net.abstracts.Channel;
import org.rapidoid.net.impl.RapidoidHelper;

public class RapidoidHttpServer extends AbstractAutumnHttpServer {
    private Rapidoid rapidoid = new Rapidoid();
    private Server listeningServer;

    public RapidoidHttpServer(String host, int port, AutumnHttpHandler handler) {
        super(host, port, handler);
    }

    @Override
    public synchronized void start() throws AutumnHttpException {
        stop();
        listeningServer = rapidoid.listen(host, port);
    }

    @Override
    public synchronized void stop() {
        if (listeningServer != null && listeningServer.isActive()) {
            listeningServer.shutdown();
        }
        listeningServer = null;
    }

    class Rapidoid extends AbstractHttpServer {
        @Override
        protected HttpStatus handle(Channel ctx, Buf buf, RapidoidHelper req) {
            RPCHttpResponse response = handler.handle(RPCHttpRequest.builder()
                    .method(req.verb.str(buf))
                    .path(req.path.str(buf))
                    .body(req.isGet.value ? null : req.body.bytes(buf))
                    .build()
            );
            ctx.write(HttpResponseCodes.get(response.getCode()));
            writeCommonHeaders(ctx, true);
            ctx.write("Content-Type: " + response.getContentType() + "\r\n");
            HttpIO.INSTANCE.writeContentLengthHeader(ctx, response.getBody().length);
            ctx.write(CR_LF);
            ctx.write(response.getBody());
            return HttpStatus.DONE;
        }
    }
}
