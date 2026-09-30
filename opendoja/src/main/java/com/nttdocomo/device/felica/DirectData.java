package com.nttdocomo.device.felica;

/**
 * Holds direct binary FeliCa data.
 */
public final class DirectData extends FelicaData {
    private byte[] bytes;

    /**
     * Creates a {@code DirectData} object.
     *
     * @param bytes the direct data bytes
     * @throws NullPointerException if {@code bytes} is {@code null}
     */
    public DirectData(byte[] bytes) {
        super(TYPE_DIRECT_DATA);
        setBytes(bytes);
    }

    /**
     * Sets the direct data bytes.
     *
     * @param bytes the direct data bytes
     * @throws NullPointerException if {@code bytes} is {@code null}
     */
    public void setBytes(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes");
        }
        this.bytes = bytes.clone();
    }

    /**
     * Gets the direct data bytes.
     *
     * @return a copy of the direct data bytes
     */
    public byte[] getBytes() {
        return bytes.clone();
    }
}
