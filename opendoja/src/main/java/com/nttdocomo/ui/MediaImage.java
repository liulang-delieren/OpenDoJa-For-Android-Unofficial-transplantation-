package com.nttdocomo.ui;

import com.nttdocomo.io.ConnectionException;

/**
 * Defines a media image.
 * This interface is used when a media resource is handled as an image.
 */
public interface MediaImage extends MediaResource {
    String MP4_VIDEOTRACK = "mp4.videotrack";
    String MP4_AUDIOTRACK = "mp4.audiotrack";
    String MP4_TEXTTRACK = "mp4.texttrack";

    int getWidth();

    int getHeight();

    Image getImage();

    ExifData getExifData();

    void setExifData(ExifData exifData);

    /**
     * Declares the start of use of this media image and specifies the reusable source and one-time-use flag.
     *
     * @param overwritten the media image whose internal area should be reused, or {@code null} to allocate new storage
     * @param useOnce {@code true} if this media image is for one-time use
     * @throws ConnectionException if an error occurs during network communication or similar processing
     */
    @Override
    void use(MediaResource overwritten, boolean useOnce) throws ConnectionException;
}
