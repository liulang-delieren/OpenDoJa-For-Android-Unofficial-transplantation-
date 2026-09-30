package opendoja.host;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class ByteStreams {
    private ByteStreams() {
    }

    public static byte[] readAll(InputStream in) throws IOException {
        if (in == null) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) >= 0) {
            if (read > 0) {
                out.write(buffer, 0, read);
            }
        }
        return out.toByteArray();
    }

    public static byte[] readN(InputStream in, int length) throws IOException {
        if (in == null || length <= 0) {
            return new byte[0];
        }
        byte[] out = new byte[length];
        int offset = 0;
        while (offset < length) {
            int read = in.read(out, offset, length - offset);
            if (read < 0) {
                break;
            }
            offset += read;
        }
        if (offset == length) {
            return out;
        }
        byte[] trimmed = new byte[offset];
        System.arraycopy(out, 0, trimmed, 0, offset);
        return trimmed;
    }
}
