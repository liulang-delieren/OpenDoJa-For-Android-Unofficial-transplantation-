package com.nttdocomo.fs.sd;

import com.nttdocomo.fs.EncryptionAttribute;

/**
 * Defines a file attribute representing SD-Binding encryption parameters.
 */
public class SDBindingEncryptionAttribute extends EncryptionAttribute {
    private static final int MIN_BLOCK_SIZE = 64;
    private static final int MAX_EXPONENT = 13;
    private static final int MAX_EFFECTIVE_BLOCK_SIZE = 65536;

    private int blockSize = -1;

    /**
     * Creates a file-attribute instance representing SD-Binding encryption
     * parameters.
     * The default block size is implementation dependent.
     */
    public SDBindingEncryptionAttribute() {
    }

    /**
     * Sets the encryption block size in bytes.
     *
     * @param blockSize the encryption block size in bytes
     * @throws IllegalArgumentException if {@code blockSize} is invalid
     */
    public void setBlockSize(int blockSize) {
        if (!isValidBlockSize(blockSize)) {
            throw new IllegalArgumentException("Invalid SD-Binding block size: " + blockSize);
        }
        this.blockSize = java.lang.Math.min(blockSize, MAX_EFFECTIVE_BLOCK_SIZE);
    }

    int getBlockSize() {
        return blockSize;
    }

    private static boolean isValidBlockSize(int blockSize) {
        int candidate = MIN_BLOCK_SIZE;
        for (int exponent = 0; exponent <= MAX_EXPONENT; exponent++) {
            if (candidate == blockSize) {
                return true;
            }
            candidate <<= 1;
        }
        return false;
    }
}
