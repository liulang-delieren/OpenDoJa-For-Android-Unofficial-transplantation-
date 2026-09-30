package com.nttdocomo.ui;

import com.nttdocomo.lang.UnsupportedOperationException;

import opendoja.compat.imageio.IIOImage;
import opendoja.compat.imageio.ImageIO;
import opendoja.compat.imageio.ImageWriteParam;
import opendoja.compat.imageio.ImageWriter;
import opendoja.compat.imageio.stream.MemoryCacheImageOutputStream;
import opendoja.compat.awt.*;
import opendoja.compat.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * Converts images to encoded formats such as JPEG.
 */
public class ImageEncoder {
    /** Attribute controlling conversion quality (=0). */
    public static final int QUALITY = 0;

    /** Quality-priority setting (=0). */
    public static final int ATTR_QUALITY_HIGH = 0;

    /** Standard quality setting (=1). */
    public static final int ATTR_QUALITY_STANDARD = 1;

    /** Data-size-priority setting (=2). */
    public static final int ATTR_QUALITY_LOW = 2;

    private final String format;
    private int quality = ATTR_QUALITY_STANDARD;

    ImageEncoder(String format) {
        this.format = format;
    }

    /**
     * Gets an {@code ImageEncoder} instance for the specified target image
     * format.
     *
     * @param format the target image format string; specify {@code "JPEG"} for
     *        JPEG conversion
     * @return the {@code ImageEncoder} instance
     * @throws NullPointerException if {@code format} is {@code null}
     * @throws UIException if the image format is not supported
     */
    public static ImageEncoder getEncoder(String format) {
        if (format == null) {
            throw new NullPointerException("format");
        }
        if (!"JPEG".equalsIgnoreCase(format)) {
            throw new UIException(UIException.UNSUPPORTED_FORMAT, "Unsupported image format: " + format);
        }
        return new ImageEncoder("JPEG");
    }

    /**
     * Sets an attribute value related to conversion.
     * If a non-controllable or non-existent attribute is specified, it is
     * ignored without doing anything.
     *
     * @param attr the attribute to set
     * @param value the value to set
     * @throws IllegalArgumentException if {@code value} is invalid for a valid
     *         attribute
     */
    public void setAttribute(int attr, int value) {
        if (attr != QUALITY) {
            return;
        }
        if (value != ATTR_QUALITY_HIGH && value != ATTR_QUALITY_STANDARD && value != ATTR_QUALITY_LOW) {
            throw new IllegalArgumentException("Invalid quality attribute value: " + value);
        }
        quality = value;
    }

    /**
     * Checks whether an attribute can be controlled.
     *
     * @param attr the attribute kind
     * @return {@code true} if the attribute can be controlled
     */
    public final boolean isAvailable(int attr) {
        return attr == QUALITY;
    }

    /**
     * Converts the contents of the specified {@link Image} to JPEG data or a
     * similar format.
     *
     * @param img the {@link Image} object to convert
     * @param x the X coordinate of the upper-left corner of the region
     * @param y the Y coordinate of the upper-left corner of the region
     * @param width the width of the region
     * @param height the height of the region
     * @return the converted image as an {@link EncodedImage}
     * @throws UnsupportedOperationException if encode is unsupported for the
     *         specified image subclass
     * @throws NullPointerException if {@code img} is {@code null}
     * @throws IllegalArgumentException if the region is invalid
     * @throws UIException if conversion fails
     */
    public EncodedImage encode(Image img, int x, int y, int width, int height) {
        if (img == null) {
            throw new NullPointerException("img");
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be positive");
        }
        BufferedImage rendered = img.renderForDisplay();
        if (rendered == null) {
            try {
                img.getGraphics();
            } catch (RuntimeException exception) {
                throw new IllegalArgumentException("Graphics cannot be acquired for the image");
            }
            rendered = img.renderForDisplay();
        }
        if (rendered == null) {
            throw new UnsupportedOperationException("Image encoding is not supported for the specified image");
        }
        return encodeRendered(rendered, x, y, width, height);
    }

    /**
     * Converts the contents of the specified {@link Canvas} to JPEG data or a
     * similar format.
     *
     * @param canvas the {@link Canvas} object to convert
     * @param x the X coordinate of the upper-left corner of the region
     * @param y the Y coordinate of the upper-left corner of the region
     * @param width the width of the region
     * @param height the height of the region
     * @return the converted image as an {@link EncodedImage}
     * @throws UnsupportedOperationException if encode is unsupported for the
     *         specified canvas subclass
     * @throws NullPointerException if {@code canvas} is {@code null}
     * @throws IllegalArgumentException if the region is invalid
     * @throws UIException if the specified canvas is not the current frame or
     *         conversion fails
     */
    public EncodedImage encode(Canvas canvas, int x, int y, int width, int height) {
        if (canvas == null) {
            throw new NullPointerException("canvas");
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be positive");
        }
        if (Display.getCurrent() != canvas) {
            throw new UIException(UIException.ILLEGAL_STATE, "Canvas must be the current frame");
        }
        if (canvas.surface() == null) {
            canvas.runtimeGraphics();
        }
        if (canvas.surface() == null) {
            throw new UnsupportedOperationException("Canvas encoding is not supported for the specified canvas");
        }
        return encodeRendered(canvas.surface().image(), x, y, width, height);
    }

    private EncodedImage encodeRendered(BufferedImage source, int x, int y, int width, int height) {
        if (x < 0 || y < 0 || x + width > source.getWidth() || y + height > source.getHeight()) {
            throw new IllegalArgumentException("The specified region exceeds the source bounds");
        }
        BufferedImage cropped = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = cropped.createGraphics();
        try {
            graphics.drawImage(source, 0, 0, width, height, x, y, x + width, y + height, null);
        } finally {
            graphics.dispose();
        }
        byte[] encoded = encodeJpeg(cropped);
        MediaImage mediaImage = new MediaManager.BasicMediaImage(cropped);
        return new EncodedImage(encoded, mediaImage);
    }

    private byte[] encodeJpeg(BufferedImage image) {
        BufferedImage rgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = rgb.createGraphics();
        try {
            graphics.drawImage(image, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
        if (!writers.hasNext()) {
            throw new UIException(UIException.UNSUPPORTED_FORMAT, "No writer available for " + format);
        }
        ImageWriter writer = writers.next();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (MemoryCacheImageOutputStream imageOutput = new MemoryCacheImageOutputStream(output)) {
            writer.setOutput(imageOutput);
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(switch (quality) {
                    case ATTR_QUALITY_HIGH -> 0.95f;
                    case ATTR_QUALITY_LOW -> 0.45f;
                    default -> 0.75f;
                });
            }
            writer.write(null, new IIOImage(rgb, null, null), param);
            writer.dispose();
            return output.toByteArray();
        } catch (IOException exception) {
            writer.dispose();
            throw new UIException(UIException.UNDEFINED, exception.getMessage());
        }
    }
}
