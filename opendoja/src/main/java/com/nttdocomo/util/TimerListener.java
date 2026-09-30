package com.nttdocomo.util;

/**
 * Defines a listener for timer events.
 * Objects that receive timer events should implement this interface.
 */
public interface TimerListener extends EventListener {
    /**
     * Called when a timer event occurs.
     *
     * @param timer the timer object
     */
    void timerExpired(Timer timer);
}
