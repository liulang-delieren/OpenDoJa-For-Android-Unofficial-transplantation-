package com.nttdocomo.ui;

import opendoja.compat.awt.image.BufferedImage;

/**
 * Host-side helpers for Image contract checks that are not part of the DoJa API surface.
 */
public final class _ImageInternalAccess {
    private _ImageInternalAccess() {
    }

    public static boolean isDisposed(Image image) {
        if (image == null) {
            return false;
        }
        try {
            image.getWidth();
            return false;
        } catch (UIException e) {
            if (e.getStatus() == UIException.ILLEGAL_STATE) {
                return true;
            }
            throw e;
        }
    }

    public static void requireUsable(Image image) {
        if (image == null) {
            return;
        }
        image.getWidth();
        image.getHeight();
    }

    public static BufferedImage copyOpaqueSourceForTransparentImage(Image image) {
        if (image == null) {
            throw new NullPointerException("image");
        }
        if (!(image instanceof DesktopImage desktopImage)) {
            // TransparentImage only accepts plain Image instances, not other extended image types.
            throw new UIException(UIException.UNSUPPORTED_FORMAT, "Extended images are not supported");
        }
        return desktopImage.copyOpaqueSourceForTransparentImage();
    }

    public static int[] copyDisplayPixels(Image image) {
        if (image == null) {
            throw new NullPointerException("image");
        }
        BufferedImage rendered = image.renderForDisplay();
        if (rendered == null) {
            throw new UIException(UIException.UNSUPPORTED_FORMAT, "Image cannot be rendered");
        }
        int width = rendered.getWidth();
        int height = rendered.getHeight();
        return rendered.getRGB(0, 0, width, height, null, 0, width);
    }
}
