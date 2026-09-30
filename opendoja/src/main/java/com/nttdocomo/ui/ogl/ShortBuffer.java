package com.nttdocomo.ui.ogl;

/**
 * Defines a short direct buffer.
 */
public interface ShortBuffer extends DirectBuffer {
    short[] get(int index, short[] buff);

    short[] get(int index, short[] buff, int offset, int length);

    void put(int index, short[] buff);

    void put(int index, short[] buff, int offset, int length);
}
