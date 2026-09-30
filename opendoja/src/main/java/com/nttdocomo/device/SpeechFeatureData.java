package com.nttdocomo.device;

/**
 * Represents speech-recognition feature data.
 */
public class SpeechFeatureData {
    private final byte[] data;
    private final boolean last;

    SpeechFeatureData(byte[] data, boolean last) {
        this.data = data == null ? new byte[0] : data;
        this.last = last;
    }

    private SpeechFeatureData() {
        this(new byte[0], false);
    }

    /**
     * Gets the feature data.
     *
     * @return the internal byte array reference
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Checks whether this feature data is the last data.
     *
     * @return {@code true} if this is the last feature data
     */
    public boolean isLast() {
        return last;
    }
}
