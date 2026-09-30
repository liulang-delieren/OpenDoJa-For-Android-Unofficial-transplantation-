package com.nttdocomo.ui;

import com.nttdocomo.io.ConnectionException;

/**
 * Defines a media sound.
 * This interface is used when a media resource is handled as audio.
 */
public interface MediaSound extends MediaResource {
    String AUDIO_3D_RESOURCES = "3d.resources";

    /**
     * Declares the start of use of this media sound and specifies the reusable source and one-time-use flag.
     *
     * @param overwritten the media sound whose internal area should be reused, or {@code null} to allocate new storage
     * @param useOnce {@code true} if this media sound is for one-time use
     * @throws ConnectionException if an error occurs during network communication or similar processing
     */
    @Override
    void use(MediaResource overwritten, boolean useOnce) throws ConnectionException;
}
