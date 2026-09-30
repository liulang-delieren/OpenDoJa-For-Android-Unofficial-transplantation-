package com.nttdocomo.device;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Provides the IR remote-controller function.
 * An application can hold the single instance of this class by using
 * {@link #getIrRemoteControl()}.
 */
public class IrRemoteControl {
    /** Indicates the High-leading output pattern (=0). */
    public static final int PATTERN_HL = 0;

    /** Indicates the Low-leading output pattern (=1). */
    public static final int PATTERN_LH = 1;

    private static final int DEFAULT_TIMEOUT_SECONDS = 10;
    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "openDoJa-ir");
        thread.setDaemon(true);
        return thread;
    });
    private static final IrRemoteControl INSTANCE = new IrRemoteControl();

    private Pulse carrier;
    private Pulse code0;
    private Pulse code1;
    private ScheduledFuture<?> sendFuture;

    /**
     * Applications cannot directly construct instances of this class.
     */
    protected IrRemoteControl() {
    }

    /**
     * Gets the IR remote-controller object.
     *
     * @return the IR remote-controller object
     */
    public static IrRemoteControl getIrRemoteControl() {
        return INSTANCE;
    }

    /**
     * Sends signals with a specified timeout and send count.
     * This method executes asynchronously and does not block.
     *
     * @param numFrames the number of frame data items to transmit
     * @param frames the frame-data array to transmit
     * @param timeout the timeout value in seconds
     * @param count the number of times to send the signal
     * @throws NullPointerException if {@code frames} is {@code null}, or if
     *         one of the referenced frames is {@code null}
     * @throws IllegalArgumentException if one of the configured pulse or frame
     *         parameters is invalid
     * @throws ArrayIndexOutOfBoundsException if {@code numFrames} is 0 or less,
     *         or if {@code frames.length} is smaller than {@code numFrames}
     * @throws DeviceException if infrared transmission is already in progress
     */
    public synchronized void send(int numFrames, IrRemoteControlFrame[] frames, int timeout, int count) {
        validateSendArguments(numFrames, frames, timeout, count);
        if (sendFuture != null && !sendFuture.isDone()) {
            throw new DeviceException(DeviceException.BUSY_RESOURCE,
                    "IR transmission is already in progress");
        }
        long timeoutMicros = TimeUnit.SECONDS.toMicros(timeout);
        long requestedMicros = sequenceMicros(numFrames, frames);
        long runMicros = requestedMicros == Long.MAX_VALUE
                ? timeoutMicros
                : java.lang.Math.min(timeoutMicros, saturatingMultiply(requestedMicros, count));
        long delayMicros = java.lang.Math.max(1L, runMicros);
        sendFuture = EXECUTOR.schedule(this::finishSend, delayMicros, TimeUnit.MICROSECONDS);
    }

    /**
     * Sends signals with a specified timeout.
     * This method executes asynchronously and does not block.
     *
     * @param numFrames the number of frame data items to transmit
     * @param frames the frame-data array to transmit
     * @param timeout the timeout value in seconds
     */
    public void send(int numFrames, IrRemoteControlFrame[] frames, int timeout) {
        validateSendArguments(numFrames, frames, timeout, 1);
        synchronized (this) {
            if (sendFuture != null && !sendFuture.isDone()) {
                throw new DeviceException(DeviceException.BUSY_RESOURCE,
                        "IR transmission is already in progress");
            }
            sendFuture = EXECUTOR.schedule(this::finishSend, java.lang.Math.max(1, timeout), TimeUnit.SECONDS);
        }
    }

    /**
     * Sends signals with the default timeout value of 10 seconds.
     * This method executes asynchronously and does not block.
     *
     * @param numFrames the number of frame data items to transmit
     * @param frames the frame-data array to transmit
     */
    public void send(int numFrames, IrRemoteControlFrame[] frames) {
        send(numFrames, frames, DEFAULT_TIMEOUT_SECONDS, 1);
    }

    /**
     * Stops infrared signal transmission.
     */
    public synchronized void stop() {
        if (sendFuture != null) {
            sendFuture.cancel(false);
            sendFuture = null;
        }
    }

    /**
     * Sets the carrier information.
     *
     * @param highDuration the carrier high duration in 0.1 microseconds
     * @param lowDuration the carrier low duration in 0.1 microseconds
     * @throws IllegalArgumentException if either duration is 0 or less
     */
    public synchronized void setCarrier(int highDuration, int lowDuration) {
        carrier = new Pulse(-1, validatePositive(highDuration, "highDuration"),
                validatePositive(lowDuration, "lowDuration"));
    }

    /**
     * Sets the logical-0 pulse information.
     *
     * @param pattern the output pattern
     * @param highDuration the logical-0 high duration in microseconds
     * @param lowDuration the logical-0 low duration in microseconds
     * @throws IllegalArgumentException if one of the arguments is invalid
     */
    public synchronized void setCode0(int pattern, int highDuration, int lowDuration) {
        code0 = new Pulse(validatePattern(pattern), validatePositive(highDuration, "highDuration"),
                validatePositive(lowDuration, "lowDuration"));
    }

    /**
     * Sets the logical-1 pulse information.
     *
     * @param pattern the output pattern
     * @param highDuration the logical-1 high duration in microseconds
     * @param lowDuration the logical-1 low duration in microseconds
     * @throws IllegalArgumentException if one of the arguments is invalid
     */
    public synchronized void setCode1(int pattern, int highDuration, int lowDuration) {
        code1 = new Pulse(validatePattern(pattern), validatePositive(highDuration, "highDuration"),
                validatePositive(lowDuration, "lowDuration"));
    }

    private synchronized void finishSend() {
        sendFuture = null;
    }

    private void validateSendArguments(int numFrames, IrRemoteControlFrame[] frames, int timeout, int count) {
        if (numFrames <= 0) {
            throw new ArrayIndexOutOfBoundsException("numFrames must be positive: " + numFrames);
        }
        Objects.requireNonNull(frames, "frames");
        if (frames.length < numFrames) {
            throw new ArrayIndexOutOfBoundsException(
                    "frames length is smaller than numFrames: " + frames.length + " < " + numFrames);
        }
        if (timeout <= 0) {
            throw new IllegalArgumentException("timeout out of range: " + timeout);
        }
        if (count <= 0) {
            throw new IllegalArgumentException("count out of range: " + count);
        }
        if (carrier == null) {
            throw new IllegalArgumentException("Carrier information has not been configured");
        }
        if (code0 == null || code1 == null) {
            throw new IllegalArgumentException("Logical pulse information has not been configured");
        }
        for (int i = 0; i < numFrames; i++) {
            IrRemoteControlFrame frame = Objects.requireNonNull(frames[i], "frames[" + i + "]");
            if (!frame.isConfigured()) {
                throw new IllegalArgumentException("Frame information is incomplete: index=" + i);
            }
            if (frame.frameDurationMicros() < frame.transmitMicros(code0, code1)) {
                throw new IllegalArgumentException("Frame duration is shorter than the transmission time: index=" + i);
            }
        }
    }

    private long sequenceMicros(int numFrames, IrRemoteControlFrame[] frames) {
        long total = 0L;
        for (int i = 0; i < numFrames; i++) {
            IrRemoteControlFrame frame = frames[i];
            if (frame.repeatCount() == IrRemoteControlFrame.COUNT_INFINITE) {
                return Long.MAX_VALUE;
            }
            total = safeAdd(total, saturatingMultiply(frame.frameDurationMicros(), frame.repeatCount()));
        }
        return total;
    }

    private static long saturatingMultiply(long value, int multiplier) {
        if (value == Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        try {
            return java.lang.Math.multiplyExact(value, (long) multiplier);
        } catch (ArithmeticException ignored) {
            return Long.MAX_VALUE;
        }
    }

    private static long safeAdd(long left, long right) {
        if (left == Long.MAX_VALUE || right == Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        try {
            return java.lang.Math.addExact(left, right);
        } catch (ArithmeticException ignored) {
            return Long.MAX_VALUE;
        }
    }

    private static int validatePattern(int pattern) {
        if (pattern != PATTERN_HL && pattern != PATTERN_LH) {
            throw new IllegalArgumentException("pattern out of range: " + pattern);
        }
        return pattern;
    }

    private static int validatePositive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " out of range: " + value);
        }
        return value;
    }

    record Pulse(int pattern, int highDuration, int lowDuration) {
    }
}
