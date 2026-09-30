package opendoja.compat.imageio;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;

import opendoja.compat.awt.image.BufferedImage;
import opendoja.compat.awt.image.IndexColorModel;
import opendoja.compat.awt.image.RenderedImage;
import opendoja.compat.awt.image.WritableRaster;

public final class ImageIO {
    private ImageIO() {
    }

    /**
     * Decodes an image through {@link BitmapFactory}. GIF input additionally exposes the
     * color table as an {@link IndexColorModel} plus an index raster so PalettedImage can
     * apply runtime palette swaps (indices are recovered by reverse lookup from the
     * decoded pixels).
     */
    public static BufferedImage read(InputStream input) throws IOException {
        if (input == null) {
            return null;
        }
        return decode(readAll(input));
    }

    static BufferedImage decode(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        if (bitmap == null) {
            return null;
        }
        if (isGif(data)) {
            GifPalette gif = GifPalette.parse(data);
            if (gif != null) {
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                int[] pixels = new int[width * height];
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
                int[] indices = gif.mapToIndices(pixels);
                WritableRaster raster = new WritableRaster(width, height, indices);
                IndexColorModel model = new IndexColorModel(8, gif.size, gif.red, gif.green, gif.blue,
                        gif.transparentIndex);
                return new BufferedImage(bitmap, model, raster);
            }
        }
        return new BufferedImage(bitmap);
    }

    public static boolean write(RenderedImage image, String formatName, OutputStream output) throws IOException {
        if (image == null || output == null) {
            return false;
        }
        if (!(image instanceof BufferedImage buffered)) {
            return false;
        }
        Bitmap bitmap = buffered.getBitmap();
        if (bitmap == null) {
            return false;
        }
        return bitmap.compress(toCompressFormat(formatName), 100, output);
    }

    public static Iterator<ImageWriter> getImageWritersByFormatName(String formatName) {
        String normalized = formatName == null ? "" : formatName.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("png") || normalized.equals("jpg") || normalized.equals("jpeg")) {
            return Collections.singletonList(new ImageWriter(normalized)).iterator();
        }
        return Collections.emptyIterator();
    }

    public static Iterator<ImageReader> getImageReaders(Object input) {
        if (input == null) {
            return Collections.emptyIterator();
        }
        return Collections.singletonList(new ImageReader()).iterator();
    }

    public static opendoja.compat.imageio.stream.ImageInputStream createImageInputStream(Object input) throws IOException {
        if (input == null) {
            return null;
        }
        byte[] data;
        if (input instanceof byte[] bytes) {
            data = bytes;
        } else if (input instanceof InputStream stream) {
            data = readAll(stream);
        } else if (input instanceof File file) {
            try (InputStream stream = new FileInputStream(file)) {
                data = readAll(stream);
            }
        } else {
            return null;
        }
        return new opendoja.compat.imageio.stream.ByteArrayImageInputStream(data);
    }

    private static Bitmap.CompressFormat toCompressFormat(String formatName) {
        String normalized = formatName == null ? "" : formatName.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("jpg") || normalized.equals("jpeg")) {
            return Bitmap.CompressFormat.JPEG;
        }
        return Bitmap.CompressFormat.PNG;
    }

    private static byte[] readAll(InputStream input) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = input.read(buffer)) >= 0) {
            if (read > 0) {
                out.write(buffer, 0, read);
            }
        }
        return out.toByteArray();
    }

    private static boolean isGif(byte[] data) {
        return data.length > 5
                && data[0] == 'G' && data[1] == 'I' && data[2] == 'F';
    }

    /** GIF global/first-image color table plus transparency index. */
    private static final class GifPalette {
        final byte[] red;
        final byte[] green;
        final byte[] blue;
        final int size;
        final int transparentIndex;

        private GifPalette(byte[] red, byte[] green, byte[] blue, int size, int transparentIndex) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.size = size;
            this.transparentIndex = transparentIndex;
        }

        static GifPalette parse(byte[] data) {
            if (data.length < 14) {
                return null;
            }
            int packed = data[10] & 0xFF;
            int offset = 13;
            byte[] red = null;
            byte[] green = null;
            byte[] blue = null;
            int size = 0;
            if ((packed & 0x80) != 0) {
                size = 2 << (packed & 7);
                if (data.length < offset + size * 3) {
                    return null;
                }
                red = new byte[size];
                green = new byte[size];
                blue = new byte[size];
                for (int i = 0; i < size; i++) {
                    red[i] = data[offset + i * 3];
                    green[i] = data[offset + i * 3 + 1];
                    blue[i] = data[offset + i * 3 + 2];
                }
                offset += size * 3;
            }
            int transparent = -1;
            while (offset < data.length) {
                int block = data[offset] & 0xFF;
                if (block == 0x21) {
                    if (offset + 1 >= data.length) {
                        break;
                    }
                    int label = data[offset + 1] & 0xFF;
                    int cursor = offset + 2;
                    if (label == 0xF9 && cursor < data.length) {
                        int subLength = data[cursor] & 0xFF;
                        if (subLength >= 4 && cursor + subLength < data.length) {
                            int gcePacked = data[cursor + 1] & 0xFF;
                            if ((gcePacked & 0x01) != 0) {
                                transparent = data[cursor + 4] & 0xFF;
                            }
                        }
                    }
                    while (cursor < data.length && (data[cursor] & 0xFF) != 0) {
                        cursor += 1 + (data[cursor] & 0xFF);
                    }
                    if (cursor < data.length) {
                        cursor++;
                    }
                    offset = cursor;
                } else if (block == 0x2C) {
                    if (offset + 10 > data.length) {
                        break;
                    }
                    int imagePacked = data[offset + 9] & 0xFF;
                    if ((imagePacked & 0x80) != 0) {
                        size = 2 << (imagePacked & 7);
                        int start = offset + 10;
                        if (data.length < start + size * 3) {
                            break;
                        }
                        red = new byte[size];
                        green = new byte[size];
                        blue = new byte[size];
                        for (int i = 0; i < size; i++) {
                            red[i] = data[start + i * 3];
                            green[i] = data[start + i * 3 + 1];
                            blue[i] = data[start + i * 3 + 2];
                        }
                    }
                    break;
                } else {
                    break;
                }
            }
            if (red == null || size <= 0) {
                return null;
            }
            return new GifPalette(red, green, blue, size, transparent);
        }

        /**
         * Recovers palette indices from decoded sRGB pixels: exact color match first,
         * nearest color as fallback; fully transparent pixels map to the transparent index.
         */
        int[] mapToIndices(int[] pixels) {
            int[] argb = new int[size];
            for (int i = 0; i < size; i++) {
                argb[i] = 0xFF000000 | ((red[i] & 0xFF) << 16) | ((green[i] & 0xFF) << 8) | (blue[i] & 0xFF);
            }
            int[] indices = new int[pixels.length];
            for (int p = 0; p < pixels.length; p++) {
                int color = pixels[p];
                if ((color >>> 24) == 0) {
                    indices[p] = transparentIndex >= 0 && transparentIndex < size ? transparentIndex : 0;
                    continue;
                }
                int wanted = color | 0xFF000000;
                int found = -1;
                for (int i = 0; i < size && found < 0; i++) {
                    if (i == transparentIndex) {
                        continue;
                    }
                    if (argb[i] == wanted) {
                        found = i;
                    }
                }
                if (found < 0) {
                    int bestDistance = Integer.MAX_VALUE;
                    int redChannel = (color >> 16) & 0xFF;
                    int greenChannel = (color >> 8) & 0xFF;
                    int blueChannel = color & 0xFF;
                    for (int i = 0; i < size; i++) {
                        if (i == transparentIndex) {
                            continue;
                        }
                        int dr = redChannel - ((argb[i] >> 16) & 0xFF);
                        int dg = greenChannel - ((argb[i] >> 8) & 0xFF);
                        int db = blueChannel - (argb[i] & 0xFF);
                        int distance = dr * dr + dg * dg + db * db;
                        if (distance < bestDistance) {
                            bestDistance = distance;
                            found = i;
                        }
                    }
                    if (found < 0) {
                        found = 0;
                    }
                }
                indices[p] = found;
            }
            return indices;
        }
    }
}
