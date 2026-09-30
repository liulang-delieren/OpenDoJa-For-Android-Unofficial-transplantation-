package com.nttdocomo.ui.ogl;

/**
 * Defines a byte direct buffer.
 */
public interface ByteBuffer extends DirectBuffer {
    byte[] get(int index, byte[] buff);

    byte[] get(int index, byte[] buff, int offset, int length);

    void put(int index, byte[] buff);

    void put(int index, byte[] buff, int offset, int length);
}
