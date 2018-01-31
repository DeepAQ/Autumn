package cn.imaq.autumn.http.protocol;

public class AutumnByteArrayReader {
    private byte[] src;
    private int offset;
    private int limit;

    private int pos;
    private int lineStart;

    public AutumnByteArrayReader(byte[] src) {
        this(src, 0, src.length);
    }

    public AutumnByteArrayReader(byte[] src, int offset, int limit) {
        this.src = src;
        this.offset = offset;
        this.limit = limit;

        this.pos = offset;
        this.lineStart = offset;
    }

    public String nextLine() {
        while (pos < offset + limit - 1) {
            if (src[pos] == '\r' && src[pos + 1] == '\n') {
                String line = new String(src, lineStart, pos - lineStart);
                pos += 2;
                lineStart = pos;
                return line;
            }
            pos++;
        }
//        if (pos < offset + limit) {
//            pos++;
//            return new String(src, lineStart, pos - lineStart);
//        }
        return null;
    }

    public int getReadBytes() {
        return lineStart - offset;
    }
}
