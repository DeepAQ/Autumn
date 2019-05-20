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
    private InetSocketAddress remote;
    private Selector selector;
    private SocketChannel channel;
    private ByteBuffer buf;

    public HttpConnection(InetSocketAddress remote) throws IOException {
        this.remote = remote;
        selector = Selector.open();
        channel = SocketChannel.open();
        channel.configureBlocking(false);
        buf = ByteBuffer.allocateDirect(1024);
    }

    public void checkConnected(long deadline) throws IOException {
        if (!channel.isConnected()) {
            channel.register(selector, SelectionKey.OP_CONNECT);
            channel.connect(remote);
            while (true) {
                int count = selector.select(deadline - System.currentTimeMillis());
                if (count <= 0) {
                    channel.close();
                    throw new IOException("Connect timed out: " + remote);
                }
                Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
                while (keyIter.hasNext()) {
                    SelectionKey key = keyIter.next();
                    keyIter.remove();
                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isConnectable()) {
                        if (!channel.finishConnect()) {
                            throw new IOException("Failed to connect: " + remote);
                        }
                        channel.register(selector, SelectionKey.OP_READ);
                        return;
                    }
                }
            }
        }
    }

    public void writeBytes(byte[] data) throws IOException {
        channel.write(ByteBuffer.wrap(data));
    }

    public AutumnHttpResponse readResponse(long deadline) throws IOException {
        HttpClientSession session = new HttpClientSession();
        while (true) {
            int count = selector.select(deadline - System.currentTimeMillis());
            if (count <= 0) {
                channel.close();
                selector.close();
                throw new IOException("Read timed out: " + remote);
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
