package opendoja.compat.imageio;

import java.io.IOException;
import java.io.InputStream;

public class ImageReader {
    private byte[] data;

    public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {
        data = null;
        if (input == null) {
            return;
        }
        if (input instanceof opendoja.compat.imageio.stream.ByteArrayImageInputStream stream) {
            data = stream.getData();
        } else if (input instanceof byte[] bytes) {
            data = bytes;
        } else if (input instanceof InputStream inputStream) {
            try {
                java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int read;
                while ((read = inputStream.read(buffer)) >= 0) {
                    if (read > 0) {
                        out.write(buffer, 0, read);
                    }
                }
                data = out.toByteArray();
            } catch (IOException exception) {
                data = null;
            }
        }
    }

    public opendoja.compat.imageio.metadata.IIOMetadata getStreamMetadata() throws IOException {
        return null;
    }

    /**
     * Decodes the first frame. Returning {@code null} for follow-up indices ends the frame
     * loop in MediaManager (animated GIFs currently play their first frame only).
     */
    public opendoja.compat.awt.image.BufferedImage read(int imageIndex) throws IOException {
        if (data == null) {
            throw new IOException("No image input set");
        }
        if (imageIndex != 0) {
            return null;
        }
        opendoja.compat.awt.image.BufferedImage image = ImageIO.decode(data);
        if (image == null) {
            throw new IOException("Unsupported image format");
        }
        return image;
    }

    public opendoja.compat.imageio.metadata.IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        return null;
    }

    public void dispose() {
        data = null;
    }
}
