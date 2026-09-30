package com.nttdocomo.device;

import java.util.Arrays;

/**
 * Defines a data frame sent by the IR remote controller.
 * Before a frame is sent, the application must explicitly set all of the
 * required parameters through the corresponding methods.
 */
public class IrRemoteControlFrame {
    /** Indicates an infinite repeat count for a transmission unit (=0). */
    public static final int COUNT_INFINITE = 0;

    private byte[] frameData;
    private int bitLength = -1;
    private int repeatCount = -1;
    private int frameDuration = -1;
    private Integer startHighDuration;
    private Integer startLowDuration;
    private Integer stopHighDuration;

    /**
     * Creates an instance of the frame-data object.
     */
    public IrRemoteControlFrame() {
    }

    /**
     * Sets the data section of the frame from two up-to-64-bit values.
     *
     * @param data1 the first up to 64 bits of transmission data
     * @param bitLength1 the valid bit length of {@code data1} from the most
     *        significant bit
     * @param data2 the second up to 64 bits of transmission data
     * @param bitLength2 the valid bit length of {@code data2} from the most
     *        significant bit
     * @throws IllegalArgumentException if {@code bitLength1} or
     *         {@code bitLength2} is outside the valid range
     */
    public void setFrameData(long data1, int bitLength1, long data2, int bitLength2) {
        validateLongBitLength(bitLength1, "bitLength1");
        validateLongBitLength(bitLength2, "bitLength2");
        byte[] bytes = new byte[16];
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) (data1 >>> (56 - i * 8));
            bytes[8 + i] = (byte) (data2 >>> (56 - i * 8));
        }
        this.frameData = bytes;
        this.bitLength = bitLength1 + bitLength2;
    }

    /**
     * Sets the data section of the frame from a byte array.
     *
     * @param data the byte array containing the transmission data
     * @param bitLength the valid bit length of the transmission data
     * @throws NullPointerException if {@code data} is {@code null}
     * @throws IllegalArgumentException if {@code bitLength} is negative or
     *         greater than {@code data.length * 8}
     */
    public void setFrameData(byte[] data, int bitLength) {
        if (data == null) {
            throw new NullPointerException("data");
        }
        if (bitLength < 0 || bitLength > data.length * 8) {
            throw new IllegalArgumentException("bitLength out of range: " + bitLength);
        }
        this.frameData = Arrays.copyOf(data, data.length);
        this.bitLength = bitLength;
    }

    /**
     * Sets the repeat count of the frame.
     *
     * @param count the repeat count of the frame
     * @throws IllegalArgumentException if {@code count} is negative
     */
    public void setRepeatCount(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count out of range: " + count);
        }
        this.repeatCount = count;
    }

    /**
     * Sets the repeat period of the frame in units of 0.1 milliseconds.
     *
     * @param duration the repeat period of the frame
     * @throws IllegalArgumentException if {@code duration} is 0 or less
     */
    public void setFrameDuration(int duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException("duration out of range: " + duration);
        }
        this.frameDuration = duration;
    }

    /**
     * Sets the high duration of the frame start section in microseconds.
     *
     * @param duration the high duration of the frame start section
     * @throws IllegalArgumentException if {@code duration} is negative
     */
    public void setStartHighDuration(int duration) {
        if (duration < 0) {
            throw new IllegalArgumentException("duration out of range: " + duration);
        }
        this.startHighDuration = duration;
    }

    /**
     * Sets the low duration of the frame start section in microseconds.
     *
     * @param duration the low duration of the frame start section
     * @throws IllegalArgumentException if {@code duration} is negative
     */
    public void setStartLowDuration(int duration) {
        if (duration < 0) {
            throw new IllegalArgumentException("duration out of range: " + duration);
        }
        this.startLowDuration = duration;
    }

    /**
     * Sets the high duration of the frame stop section in microseconds.
     *
     * @param duration the high duration of the frame stop section
     * @throws IllegalArgumentException if {@code duration} is negative
     */
    public void setStopHighDuration(int duration) {
        if (duration < 0) {
            throw new IllegalArgumentException("duration out of range: " + duration);
        }
        this.stopHighDuration = duration;
    }

    boolean isConfigured() {
        return frameData != null
                && bitLength >= 0
                && repeatCount >= 0
                && frameDuration > 0
                && startHighDuration != null
                && startLowDuration != null
                && stopHighDuration != null;
    }

    int repeatCount() {
        return repeatCount;
    }

    long transmitMicros(IrRemoteControl.Pulse code0, IrRemoteControl.Pulse code1) {
        if (!isConfigured()) {
            return Long.MAX_VALUE;
        }
        long total = startHighDuration.longValue() + startLowDuration.longValue() + stopHighDuration.longValue();
        for (int bit = 0; bit < bitLength; bit++) {
            boolean value = bitAt(bit);
            IrRemoteControl.Pulse pulse = value ? code1 : code0;
            total += pulse.highDuration() + pulse.lowDuration();
        }
        return total;
    }

    long frameDurationMicros() {
        return frameDuration < 0 ? Long.MIN_VALUE : frameDuration * 100L;
    }

    private boolean bitAt(int bitIndex) {
        int byteIndex = bitIndex / 8;
        int bitOffset = 7 - (bitIndex % 8);
        return ((frameData[byteIndex] >>> bitOffset) & 0x01) != 0;
    }

    private static void validateLongBitLength(int bitLength, String name) {
        if (bitLength < 0 || bitLength > 64) {
            throw new IllegalArgumentException(name + " out of range: " + bitLength);
        }
    }
}
