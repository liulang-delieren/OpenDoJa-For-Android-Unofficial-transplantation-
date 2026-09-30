package com.nttdocomo.util;

/**
 * Defines the timer interface.
 * This interface specifies the interface that a class implementing a timer
 * should have.
 * A timer implementation generates a timer event when the specified time has
 * elapsed and notifies the application of that event by some means.
 * The way the timer-event interval is specified and the way the event is
 * delivered are not defined by this interface.
 *
 * <p>Since DoJa-4.0 (901i), this interface also provides methods for obtaining
 * device-dependent timing information for implementations that cannot generate
 * timer events exactly at the interval specified by the application.
 * The implementation-supported timer-event intervals are modeled as an
 * arithmetic progression whose first term is returned by
 * {@link #getMinTimeInterval()} and whose common difference is returned by
 * {@link #getResolution()}.</p>
 */
public interface TimeKeeper {
    /**
     * Disposes this timer.
     */
    void dispose();

    /**
     * Gets the minimum timer-event interval supported by the implementation.
     *
     * @return the minimum supported timer-event interval, in milliseconds
     */
    int getMinTimeInterval();

    /**
     * Gets the timer resolution.
     *
     * @return the timer resolution, in milliseconds
     */
    int getResolution();

    /**
     * Starts the timer.
     */
    void start();

    /**
     * Stops the timer.
     */
    void stop();
}
