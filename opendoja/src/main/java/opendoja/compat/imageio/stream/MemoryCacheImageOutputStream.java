package opendoja.compat.imageio.stream;

import java.io.IOException;
import java.io.OutputStream;

public class MemoryCacheImageOutputStream implements ImageInputStream {
    private final OutputStream stream;

    public MemoryCacheImageOutputStream(OutputStream stream) {
        this.stream = stream;
    }

    public OutputStream getOutputStream() {
        return stream;
    }

    @Override
    public void close() throws IOException {
        if (stream != null) {
            stream.flush();
        }
    }
}
