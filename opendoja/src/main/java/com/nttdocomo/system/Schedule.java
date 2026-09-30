package com.nttdocomo.system;

import com.nttdocomo.util.ScheduleDate;

/**
 * Provides access to the terminal's native scheduler.
 */
public final class Schedule {
    private Schedule() {
    }

    /**
     * Gets the supported schedule-time types.
     *
     * @return the logical OR of the supported {@link ScheduleDate} types
     */
    public static int getSupportedTypes() {
        return _SystemSupport.supportedScheduleTypes();
    }

    /**
     * Registers a schedule entry through native-style user interaction.
     *
     * @param description the description text
     * @param date the schedule date and time, or {@code null}
     * @param alarm whether an alarm should sound
     * @return {@code true} if registration succeeds
     * @throws InterruptedOperationException declared by the API contract
     */
    public static boolean addEntry(String description, ScheduleDate date, boolean alarm)
            throws InterruptedOperationException {
        return _SystemSupport.addSchedule(description, date, alarm);
    }
}
