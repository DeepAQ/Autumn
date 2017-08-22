package cn.imaq.autumn.http.client.protocol;

import java.io.ByteArrayOutputStream;
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

    public HttpConnection(InetSocketAddress remote) throws IOException {
        selector = Selector.open();
        channel = SocketChannel.open(remote);
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
    }

    public boolean isAvailable() {
        return channel.isConnected();
    }

    public byte[] writeThenRead(byte[] data, int timeoutMillis) throws IOException {
        // write data
        channel.write(ByteBuffer.wrap(data));
        // read response
        int count = selector.select(timeoutMillis);
        if (count <= 0) {
            throw new IOException("Read timed out: " + channel.getRemoteAddress());
        }
        Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
        while (keyIter.hasNext()) {
            SelectionKey key = keyIter.next();
            keyIter.remove();
            if (!key.isValid()) {
                continue;
            }
            if (key.isReadable() && key.channel() instanceof SocketChannel) {
                ByteBuffer buf = ByteBuffer.allocate(1024);
                ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
                while (true) {
                    int readBytes = ((SocketChannel) key.channel()).read(buf);
                    if (readBytes > 0) {
                        byte[] bytes = new byte[readBytes];
                        buf.flip();
                        buf.get(bytes);
                        os.write(bytes);
                    }
                    if (readBytes < buf.capacity()) {
                        break;
                    }
                    buf.clear();
                }
                return os.toByteArray();
            }
        }
        throw new IOException("Cannot read response from server: " + channel.getRemoteAddress());
    }
}
