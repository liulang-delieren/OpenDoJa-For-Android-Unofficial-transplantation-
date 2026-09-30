package com.nttdocomo.opt.ui;

import com.nttdocomo.ui.Graphics;
import com.nttdocomo.ui.Image;
import com.nttdocomo.ui._ImageInternalAccess;
import com.nttdocomo.lang.UnsupportedOperationException;

import opendoja.compat.awt.image.BufferedImage;

/**
 * Defines an image with adjustable transparency.
 */
public class TransparentImage extends Image {
    private final Image delegate;
    private boolean transparentEnabled;

    /**
     * Applications cannot create this object directly.
     */
    protected TransparentImage() {
        this.delegate = Image.createImage(1, 1);
    }

    /**
     * Creates a transparent image by copying the specified source image.
     *
     * @param image the source image
     * @return the new transparent image
     */
    public static TransparentImage createTransparentImage(Image image) {
        if (image == null) {
            throw new NullPointerException("image");
        }
        return new TransparentImage(image);
    }

    private TransparentImage(Image source) {
        // TransparentImage must ignore the source Image's active alpha and transparent-color state.
        // Copy raw opaque pixels instead of drawing the source through Image.renderForDisplay().
        BufferedImage opaqueSource = _ImageInternalAccess.copyOpaqueSourceForTransparentImage(source);
        int width = opaqueSource.getWidth();
        int height = opaqueSource.getHeight();
        int[] pixels = opaqueSource.getRGB(0, 0, width, height, null, 0, width);
        this.delegate = Image.createImage(width, height, pixels, 0);
    }

    /**
     * Enables or disables transparent-color processing.
     *
     * @param enabled {@code true} to enable transparent-color processing
     */
    public void setTransparentEnabled(boolean enabled) {
        ensureActive();
        transparentEnabled = enabled;
        delegate.setTransparentEnabled(enabled);
    }

    /**
     * Sets the transparent color.
     *
     * @param color the transparent color
     */
    public void setTransparentColor(int color) {
        ensureActive();
        delegate.setTransparentColor(color);
        if (transparentEnabled) {
            delegate.setTransparentEnabled(true);
        }
    }

    /**
     * Returns the transparent color.
     *
     * @return the transparent color
     */
    public int getTransparentColor() {
        throw new UnsupportedOperationException("TransparentImage transparent color queries are not supported");
    }

    /**
     * Sets the alpha value.
     *
     * @param alpha the alpha value
     */
    public void setAlpha(int alpha) {
        throw new UnsupportedOperationException("TransparentImage alpha is not supported");
    }

    /**
     * Returns the alpha value.
     *
     * @return the alpha value
     */
    public int getAlpha() {
        throw new UnsupportedOperationException("TransparentImage alpha is not supported");
    }

    /**
     * Returns a graphics object for drawing onto the image.
     *
     * @return the graphics object
     */
    public final Graphics getGraphics() {
        throw new UnsupportedOperationException("TransparentImage graphics are not supported");
    }

    /**
     * Disposes this object and releases its resources.
     */
    @Override
    public void dispose() {
        delegate.dispose();
    }

    /**
     * Gets width.
     */
    @Override
    public int getWidth() {
        return delegate.getWidth();
    }

    /**
     * Gets height.
     */
    @Override
    public int getHeight() {
        return delegate.getHeight();
    }

    @Override
    protected BufferedImage renderForDisplayImpl() {
        return renderImage(delegate);
    }

    private void ensureActive() {
        delegate.getWidth();
    }
}
