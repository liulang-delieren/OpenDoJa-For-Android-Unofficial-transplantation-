package opendoja.compat.sound.sampled;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class AudioInputStream extends InputStream {
    private final InputStream stream;
    private final AudioFormat format;
    private final long frameLength;

    public AudioInputStream() {
        this(new ByteArrayInputStream(new byte[0]), null, 0L);
    }

    public AudioInputStream(InputStream stream, AudioFormat format, long frameLength) {
        this.stream = stream;
        this.format = format;
        this.frameLength = frameLength;
    }

    public AudioFormat getFormat() {
        return format;
    }

    public long getFrameLength() {
        return frameLength;
    }

    @Override
    public int read() throws IOException {
        return stream.read();
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        return stream.read(buffer, offset, length);
    }

    @Override
    public long skip(long byteCount) throws IOException {
        return stream.skip(byteCount);
    }

    @Override
    public int available() throws IOException {
        return stream.available();
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }
}
