package com.nttdocomo.util;

import com.nttdocomo.ui.UIException;
import opendoja.host.DoJaRuntime;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Timer class.
 * Both one-shot timers and interval timers are supported.
 * Timer events are delivered to a registered {@link TimerListener}.
 */
public final class Timer implements TimeKeeper {
    private static final int MIN_TIME_INTERVAL =
            java.lang.Math.max(1, opendoja.host.OpenDoJaLaunchArgs.getInt(opendoja.host.OpenDoJaLaunchArgs.TIMER_MIN_TIME_INTERVAL));
    private static final int RESOLUTION =
            java.lang.Math.max(1, opendoja.host.OpenDoJaLaunchArgs.getInt(opendoja.host.OpenDoJaLaunchArgs.TIMER_RESOLUTION));
    private boolean repeat;
    private int time;
    private TimerListener listener;
    private ScheduledFuture<?> future;
    private boolean disposed;

    /**
     * Creates a timer object.
     * The timer is initialized to one-shot mode, a 0 ms interval, and no listener.
     */
    public Timer() {
    }

    /**
     * Gets the smallest supported timer interval.
     *
     * @return the smallest supported timer interval in milliseconds
     */
    @Override
    public int getMinTimeInterval() {
        ensureUsable();
        return MIN_TIME_INTERVAL;
    }

    /**
     * Gets the timer resolution.
     *
     * @return the timer resolution in milliseconds
     */
    @Override
    public int getResolution() {
        ensureUsable();
        return RESOLUTION;
    }

    /**
     * Sets whether timer events should repeat.
     *
     * @param repeat {@code true} to repeat, {@code false} for one-shot
     * @throws UIException if the timer has already been started
     */
    public void setRepeat(boolean repeat) {
        ensureUsable();
        ensureNotStarted();
        this.repeat = repeat;
    }

    /**
     * Sets the timer interval in milliseconds.
     *
     * @param time the timer interval in milliseconds
     * @throws UIException if the timer has already been started
     * @throws IllegalArgumentException if {@code time} is negative
     */
    public void setTime(int time) {
        ensureUsable();
        ensureNotStarted();
        if (time < 0) {
            throw new IllegalArgumentException("time must be >= 0");
        }
        this.time = normalizeInterval(time);
    }

    /**
     * Sets the listener that receives timer events.
     *
     * @param listener the listener, or {@code null} to unregister it
     */
    public void setListener(TimerListener listener) {
        ensureUsable();
        this.listener = listener;
    }

    /**
     * Starts the timer.
     *
     * @throws UIException if the timer has already been started
     */
    @Override
    public void start() {
        ensureUsable();
        ensureNotStarted();
        DoJaRuntime runtime = DoJaRuntime.current();
        if (runtime == null || listener == null) {
            return;
        }
        Runnable task = () -> {
            try {
                listener.timerExpired(this);
            } finally {
                if (!repeat) {
                    synchronized (this) {
                        future = null;
                    }
                }
            }
        };
        if (repeat) {
            int interval = normalizeInterval(time);
            future = runtime.scheduler().scheduleWithFixedDelay(task, interval, interval, TimeUnit.MILLISECONDS);
        } else {
            future = runtime.scheduler().schedule(task, time, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Stops the timer.
     * If the timer is already stopped, nothing is done.
     */
    @Override
    public void stop() {
        ensureUsable();
        if (future != null) {
            future.cancel(false);
            future = null;
        }
    }

    /**
     * Disposes this timer.
     * After disposal, only {@code dispose()} itself may be called again.
     */
    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        stop();
        listener = null;
        disposed = true;
    }

    private void ensureUsable() {
        if (disposed) {
            throw new UIException(UIException.ILLEGAL_STATE, "Timer is disposed");
        }
    }

    private void ensureNotStarted() {
        if (future != null && !future.isDone()) {
            throw new UIException(UIException.ILLEGAL_STATE, "Timer is already started");
        }
    }

    private static int normalizeInterval(int interval) {
        if (interval <= MIN_TIME_INTERVAL) {
            return MIN_TIME_INTERVAL;
        }
        long steps = (long) (interval - MIN_TIME_INTERVAL + RESOLUTION - 1) / RESOLUTION;
        long normalized = MIN_TIME_INTERVAL + steps * (long) RESOLUTION;
        return normalized > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) normalized;
    }
}
