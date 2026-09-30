package opendoja.compat.imageio.stream;

/**
 * In-memory {@link ImageInputStream} used by {@code ImageIO.createImageInputStream}.
 * The reader side extracts the captured bytes via {@link #getData()}.
 */
public final class ByteArrayImageInputStream implements ImageInputStream {
    private final byte[] data;

    public ByteArrayImageInputStream(byte[] data) {
        this.data = data == null ? new byte[0] : data;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public void close() {
    }
}
