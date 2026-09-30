package com.nttdocomo.device.location;

import com.nttdocomo.util.EventListener;

/**
 * Called when the acceleration-sensor function detects an event.
 */
public interface AccelerationEventListener extends EventListener {
    /**
     * Called when the acceleration-sensor function detects an event.
     *
     * @param sensor the acceleration-sensor instance that detected the event
     * @param type the detected event type
     * @param value the detected event value
     */
    void actionPerformed(AccelerationSensor sensor, int type, int value);
}
