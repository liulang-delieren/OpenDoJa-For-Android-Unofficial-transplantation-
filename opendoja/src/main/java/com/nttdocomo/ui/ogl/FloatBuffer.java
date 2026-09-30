package com.nttdocomo.ui.ogl;

import com.nttdocomo.ui.ogl.math.Matrix4f;

/**
 * Defines a float direct buffer.
 */
public interface FloatBuffer extends DirectBuffer {
    float[] get(int index, float[] buff);

    float[] get(int index, float[] buff, int offset, int length);

    void put(int index, float[] buff);

    void put(int index, float[] buff, int offset, int length);

    FloatBuffer madd(FloatBuffer src1, FloatBuffer src2, float multiplier);

    FloatBuffer transform(FloatBuffer src, Matrix4f matrix, int itemSize, int itemCount);
}
