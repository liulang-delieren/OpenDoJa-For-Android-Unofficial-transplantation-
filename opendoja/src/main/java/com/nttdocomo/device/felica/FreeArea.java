package com.nttdocomo.device.felica;

import java.io.IOException;
import java.util.Arrays;

/**
 * Provides access to the FeliCa free area.
 */
public final class FreeArea {
    private static final int BLOCK_COUNT = 4;
    private static final int BLOCK_SIZE = 16;
    private static final int TOTAL_SIZE = BLOCK_COUNT * BLOCK_SIZE;

    private byte[] data = new byte[TOTAL_SIZE];
    private boolean reset;

    FreeArea() {
    }

    /**
     * Reads the free-area data bound to the application that uses the free
     * area.
     * The returned data length is always 64 bytes.
     *
     * @return the free-area data
     * @throws FelicaException if free-area access fails
     * @throws IOException if I/O fails
     */
    public byte[] read() throws FelicaException, IOException {
        ensureAccessible();
        return data.clone();
    }

    /**
     * Reads free-area data by specifying block indices.
     * The block index is an integer value from {@code 0} through {@code 3},
     * and up to four indices can be specified.
     *
     * @param positions the block-index array
     * @return the free-area data for the specified blocks
     * @throws NullPointerException if {@code positions} is {@code null}
     * @throws IllegalArgumentException if the array length is {@code 0} or
     *         greater than {@code 4}
     * @throws IndexOutOfBoundsException if an index other than {@code 0}
     *         through {@code 3} is included
     * @throws FelicaException if free-area access fails
     * @throws IOException if I/O fails
     */
    public byte[] read(int[] positions) throws FelicaException, IOException {
        ensureAccessible();
        validateBlockIndexes(positions);
        byte[] result = new byte[positions.length * BLOCK_SIZE];
        for (int i = 0; i < positions.length; i++) {
            System.arraycopy(data, positions[i] * BLOCK_SIZE, result, i * BLOCK_SIZE, BLOCK_SIZE);
        }
        return result;
    }

    /**
     * Writes data to the free area bound to the application that uses the free
     * area.
     * If the data length is shorter than 64 bytes, the remaining bytes are
     * padded with zero. Any bytes beyond 64 are discarded.
     *
     * @param data the free-area data
     * @throws NullPointerException if {@code data} is {@code null}
     * @throws FelicaException if free-area access fails
     * @throws IOException if I/O fails
     */
    public void write(byte[] data) throws FelicaException, IOException {
        ensureAccessible();
        if (data == null) {
            throw new NullPointerException("data");
        }
        this.data = new byte[TOTAL_SIZE];
        System.arraycopy(data, 0, this.data, 0, java.lang.Math.min(TOTAL_SIZE, data.length));
    }

    /**
     * Writes data to the free area by specifying block indices.
     * If the specified free-area data is shorter than
     * {@code index.length * 16} bytes, the remaining bytes are zero padded.
     * Any bytes beyond that range are ignored.
     *
     * @param positions the block-index array
     * @param bytes the free-area data
     * @throws NullPointerException if an argument is {@code null}
     * @throws IllegalArgumentException if the index-array length is {@code 0}
     *         or greater than {@code 4}
     * @throws IndexOutOfBoundsException if an index other than {@code 0}
     *         through {@code 3} is included
     * @throws FelicaException if free-area access fails
     * @throws IOException if I/O fails
     */
    public void write(int[] positions, byte[] bytes) throws FelicaException, IOException {
        ensureAccessible();
        if (positions == null || bytes == null) {
            throw new NullPointerException();
        }
        validateBlockIndexes(positions);
        for (int i = 0; i < positions.length; i++) {
            int sourceOffset = i * BLOCK_SIZE;
            int copyLength = java.lang.Math.max(0, java.lang.Math.min(BLOCK_SIZE, bytes.length - sourceOffset));
            int destinationOffset = positions[i] * BLOCK_SIZE;
            Arrays.fill(data, destinationOffset, destinationOffset + BLOCK_SIZE, (byte) 0);
            if (copyLength > 0) {
                System.arraycopy(bytes, sourceOffset, data, destinationOffset, copyLength);
            }
        }
    }

    /**
     * Returns the IDm of the free area.
     * Calling this method polls the free area to acquire the IDm.
     *
     * @return the IDm (8 bytes)
     * @throws FelicaException if free-area polling fails
     * @throws IOException if I/O fails
     */
    public byte[] getIDm() throws FelicaException, IOException {
        ensureAccessible();
        return FelicaSupport.idmFor(OfflineFelica.CARD_INTERNAL, 0);
    }

    /**
     * Returns whether reset is performed after reading from or writing to the
     * free area.
     * Immediately after this object is created, the default value is
     * {@code false}.
     *
     * @return {@code true} if reset is performed; {@code false} otherwise
     */
    public boolean isReset() {
        ensureAccessible();
        return reset;
    }

    /**
     * Sets whether reset is performed after reading from or writing to the
     * free area.
     * Reset is performed to clear PIN-unlock information.
     *
     * @param reset {@code true} to perform reset; {@code false} not to perform
     *              reset
     */
    public void setReset(boolean reset) {
        ensureAccessible();
        this.reset = reset;
    }

    private static void validateBlockIndexes(int[] positions) {
        if (positions == null) {
            throw new NullPointerException("positions");
        }
        if (positions.length == 0 || positions.length > BLOCK_COUNT) {
            throw new IllegalArgumentException("positions");
        }
        for (int position : positions) {
            if (position < 0 || position >= BLOCK_COUNT) {
                throw new IndexOutOfBoundsException("position");
            }
        }
    }

    private static void ensureAccessible() {
        FelicaSupport.requireOfflineAccess();
    }
}
