package com.nttdocomo.system;

import com.nttdocomo.util.ScheduleDate;

/**
 * Provides access to the terminal's native alarm function.
 */
public final class Alarm {
    private Alarm() {
    }

    /**
     * Gets the schedule-time types that can be set for alarms.
     *
     * @return the logical OR of the supported {@link ScheduleDate} types
     */
    public static int getSupportedTypes() {
        return _SystemSupport.supportedScheduleTypes();
    }

    /**
     * Registers a new alarm through native-style user interaction.
     *
     * @param date the alarm date and time, or {@code null}
     * @return {@code true} if registration succeeds
     * @throws InterruptedOperationException declared by the API contract
     */
    public static boolean addEntry(ScheduleDate date) throws InterruptedOperationException {
        return _SystemSupport.addAlarm(date);
    }
}
