package com.nttdocomo.ui;

import com.nttdocomo.io.ConnectionException;

/**
 * Defines avatar data.
 * This interface is used when a media resource is handled as avatar data.
 */
public interface AvatarData extends MediaResource {
    /**
     * Declares the start of use of this avatar data and specifies whether the use is one-time.
     *
     * @param overwritten normally {@code null}; the argument has no effect for avatar data
     * @param useOnce {@code true} if the avatar data is used only once
     * @throws ConnectionException if an error occurs during network communication or similar processing
     */
    @Override
    void use(MediaResource overwritten, boolean useOnce) throws ConnectionException;
}
