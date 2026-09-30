package com.nttdocomo.ui.ogl;

/**
 * Defines a direct-memory buffer used by the DoJa OpenGL utility APIs.
 */
public interface DirectBuffer {
    /**
     * Returns the buffer length in elements.
     *
     * @return the element length
     */
    int length();

    /**
     * Sets the segment used as the effective data range.
     *
     * @param offset the start offset
     * @param length the segment length
     */
    void setSegment(int offset, int length);

    /**
     * Clears the current segment restriction.
     */
    void clearSegment();
}
