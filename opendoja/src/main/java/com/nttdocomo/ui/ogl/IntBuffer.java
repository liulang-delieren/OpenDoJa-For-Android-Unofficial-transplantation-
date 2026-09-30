package com.nttdocomo.ui.ogl;

/**
 * Defines an int direct buffer.
 */
public interface IntBuffer extends DirectBuffer {
    int[] get(int index, int[] buff);

    int[] get(int index, int[] buff, int offset, int length);

    void put(int index, int[] buff);

    void put(int index, int[] buff, int offset, int length);
}
