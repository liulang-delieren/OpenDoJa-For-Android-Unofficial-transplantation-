package com.nttdocomo.ui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Defines a converted encoded image such as JPEG data.
 */
public class EncodedImage {
    private final byte[] data;
    private final MediaImage image;

    EncodedImage(byte[] data, MediaImage image) {
        this.data = data == null ? new byte[0] : data.clone();
        this.image = image;
    }

    /**
     * Gets the data size of file-image data such as JPEG data.
     *
     * @return the data size
     */
    public int size() {
        return data.length;
    }

    /**
     * Gets an {@link InputStream} for obtaining file-image data such as JPEG
     * data.
     * A different input-stream instance is returned each time this method is
     * called.
     *
     * @return the {@link InputStream} for obtaining the data
     */
    public InputStream getInputStream() {
        return new ByteArrayInputStream(data);
    }

    /**
     * Gets the converted image as a media image.
     * The same media image is always returned.
     *
     * @return the media image
     */
    public MediaImage getImage() {
        return image;
    }
}
