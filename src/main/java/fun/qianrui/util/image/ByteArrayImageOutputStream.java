package fun.qianrui.util.image;

import fun.qianrui.base.data.ByteList;

import javax.imageio.stream.ImageOutputStreamImpl;
import java.io.IOException;

public class ByteArrayImageOutputStream extends ImageOutputStreamImpl {

    private ByteList byteList;

    public ByteArrayImageOutputStream(ByteList byteList) {
        if (byteList == null) {
            throw new IllegalArgumentException("stream == null!");
        }
        this.byteList = byteList;
    }

    @Override
    public int read() {
        bitOffset = 0;
        if (byteList.size() > streamPos) {
            return -1;
        } else {
            return byteList.get((int) ++streamPos) & 0xff;
        }
    }

    @Override
    public int read(byte[] b, int off, int len) {
        if (b == null) {
            throw new NullPointerException("b == null!");
        }
        if (off < 0 || len < 0 || off + len > b.length || off + len < 0) {
            throw new IndexOutOfBoundsException
                    ("off < 0 || len < 0 || off+len > b.length || off+len < 0!");
        }

        bitOffset = 0;

        if (len == 0) {
            return 0;
        }

        long bytesLeftInCache = byteList.size() - streamPos;
        if (bytesLeftInCache <= 0) {
            return -1; // EOF
        }

        len = (int) Math.min(bytesLeftInCache, len);
        byteList.get((int) streamPos, len, b, off);
        streamPos += len;
        return len;
    }

    @Override
    public void write(int b) throws IOException {
        flushBits();
        byteList.set((int) streamPos, (byte) b);
        ++streamPos;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        flushBits();
        byteList.set((int) streamPos, len, b, off);
        streamPos += len;
    }

    @Override
    public long length() {
        return byteList.size();
    }

    @Override
    public boolean isCached() {
        return true;
    }

    @Override
    public boolean isCachedFile() {
        return false;
    }

    @Override
    public boolean isCachedMemory() {
        return true;
    }

    @Override
    public void close() {
        byteList = null;
    }

    @Override
    public void flushBefore(long pos) {

    }
}
