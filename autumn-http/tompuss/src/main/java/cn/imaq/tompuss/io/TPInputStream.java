package cn.imaq.tompuss.io;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.IOException;

public class TPInputStream extends ServletInputStream {
    private byte buf[];
    private int pos;
    private int mark = 0;
    private int count;

    public TPInputStream(byte buf[]) {
        this.buf = buf;
        this.pos = 0;
        this.count = buf.length;
    }

    public synchronized int read() {
        return (pos < count) ? (buf[pos++] & 0xff) : -1;
    }

    public synchronized int read(byte b[], int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        }

        if (pos >= count) {
            return -1;
        }

        int avail = count - pos;
        if (len > avail) {
            len = avail;
        }
        if (len <= 0) {
            return 0;
        }
        System.arraycopy(buf, pos, b, off, len);
        pos += len;
        return len;
    }

    public synchronized long skip(long n) {
        long k = count - pos;
        if (n < k) {
            k = n < 0 ? 0 : n;
        }

        pos += k;
        return k;
    }

    public synchronized int available() {
        return count - pos;
    }

    public boolean markSupported() {
        return true;
    }

    public void mark(int readAheadLimit) {
        mark = pos;
    }

    public synchronized void reset() {
        pos = mark;
    }

    public void close() throws IOException {
    }

    @Override
    public boolean isFinished() {
        return (count - pos <= 0);
    }

    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        try {
            readListener.onDataAvailable();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
