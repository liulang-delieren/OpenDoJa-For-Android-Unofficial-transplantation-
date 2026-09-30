package com.nttdocomo.io;

import java.io.IOException;

/**
 * Defines the interface that random-access-capable classes must implement.
 */
public interface RandomAccessible {
    long getSize() throws IOException;

    long getPosition() throws IOException;

    void setPosition(long position) throws IOException;

    void setPositionRelative(long position) throws IOException;
}
