package cn.imaq.autumn.http.client.protocol;

import cn.imaq.autumn.http.protocol.AutumnHttpResponse;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class HttpConnection {
    private Selector selector;
    private SocketChannel channel;
    private ByteBuffer buf;

    public HttpConnection(InetSocketAddress remote) throws IOException {
        selector = Selector.open();
        channel = SocketChannel.open(remote);
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        buf = ByteBuffer.allocateDirect(1024);
    }

    public boolean isAvailable() {
        return channel.isConnected();
    }

    public void writeBytes(byte[] data) throws IOException {
        channel.write(ByteBuffer.wrap(data));
    }

    public AutumnHttpResponse readResponse(int timeoutMillis) throws IOException {
        HttpClientSession session = new HttpClientSession();
        while (true) {
            int count = selector.select(timeoutMillis);
            if (count <= 0) {
                channel.close();
                selector.close();
                throw new IOException("Read timed out: " + channel.getRemoteAddress());
            }
            Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
            while (keyIter.hasNext()) {
                SelectionKey key = keyIter.next();
                keyIter.remove();
                if (!key.isValid()) {
                    continue;
                }
                if (key.isReadable()) {
                    buf.clear();
                    int readBytes = ((SocketChannel) key.channel()).read(buf);
                    if (readBytes > 0) {
                        buf.flip();
                        session.processByteBuffer(buf);
                        if (session.isFinished()) {
                            return session.getResponse();
                        }
                    }
                }
            }
        }
    }
}
