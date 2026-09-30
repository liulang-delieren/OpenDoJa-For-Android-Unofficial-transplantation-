package opendoja.compat.imageio;

import android.graphics.Bitmap;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

import opendoja.compat.awt.image.BufferedImage;

public class ImageWriter {
    private final String format;
    private Object output;

    public ImageWriter() {
        this("png");
    }

    public ImageWriter(String format) {
        this.format = format == null ? "png" : format.trim().toLowerCase(Locale.ROOT);
    }

    public void setOutput(Object output) {
        this.output = output;
    }

    public ImageWriteParam getDefaultWriteParam() {
        return new ImageWriteParam();
    }

    public void write(opendoja.compat.awt.image.RenderedImage streamMetadata, IIOImage imageObj,
                      ImageWriteParam param) throws IOException {
        if (imageObj == null || !(imageObj.getImage() instanceof BufferedImage buffered)) {
            throw new IOException("IIOImage does not carry a BufferedImage");
        }
        Bitmap bitmap = buffered.getBitmap();
        if (bitmap == null) {
            throw new IOException("BufferedImage has no bitmap");
        }
        OutputStream target = resolveOutput();
        if (target == null) {
            throw new IOException("No output set");
        }
        boolean jpeg = format.equals("jpg") || format.equals("jpeg");
        int quality = param == null ? 100 : Math.round(param.getCompressionQuality() * 100f);
        quality = Math.max(0, Math.min(100, quality));
        if (!bitmap.compress(jpeg ? Bitmap.CompressFormat.JPEG : Bitmap.CompressFormat.PNG, quality, target)) {
            throw new IOException("Bitmap encode failed for format " + format);
        }
    }

    private OutputStream resolveOutput() {
        if (output instanceof opendoja.compat.imageio.stream.MemoryCacheImageOutputStream cache) {
            return cache.getOutputStream();
        }
        if (output instanceof OutputStream stream) {
            return stream;
        }
        return null;
    }

    public void dispose() {
        output = null;
    }
}
