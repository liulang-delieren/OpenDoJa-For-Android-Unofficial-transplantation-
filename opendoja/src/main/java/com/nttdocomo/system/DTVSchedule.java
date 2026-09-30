package com.nttdocomo.system;

import com.nttdocomo.util.ScheduleDate;

import java.util.Calendar;

/**
 * Provides access to native DTV watch/record scheduling.
 */
public final class DTVSchedule {
    /** Indicates that the frequency is unspecified (=-1). */
    public static final int FREQUENCY_NONE = -1;

    /** Indicates that the service ID is unspecified (=-1). */
    public static final int SERVICE_ID_NONE = -1;

    /** Indicates that the affiliation ID is unspecified (=-1). */
    public static final int AFFILIATION_ID_NONE = -1;

    /** Indicates a watch reservation (=0). */
    public static final int TYPE_WATCH = 0;

    /** Indicates a record reservation (=1). */
    public static final int TYPE_RECORD = 1;

    /**
     * Creates a {@code DTVSchedule} object.
     */
    public DTVSchedule() {
    }

    /**
     * Gets the supported watch-reservation schedule-time types.
     *
     * @return the logical OR of the supported {@link ScheduleDate} types
     */
    public static int getSupportedTypes() {
        return _SystemSupport.supportedScheduleTypes();
    }

    /**
     * Gets the supported schedule-time types for the specified reservation type.
     *
     * @param type the reservation type
     * @return the logical OR of the supported {@link ScheduleDate} types
     */
    public static int getSupportedTypes(int type) {
        if (type != TYPE_WATCH && type != TYPE_RECORD) {
            throw new IllegalArgumentException("type out of range: " + type);
        }
        return _SystemSupport.supportedScheduleTypes();
    }

    /**
     * Registers a watch reservation.
     *
     * @param frequency the physical channel
     * @param serviceId the service ID
     * @param serviceName the service name
     * @param startTime the start time
     * @param eventName the event name
     * @return {@code true} if registration succeeds
     * @throws InterruptedOperationException declared by the API contract
     */
    public static boolean addEntry(int frequency, int serviceId, String serviceName,
                                   ScheduleDate startTime, String eventName) throws InterruptedOperationException {
        if (startTime == null) {
            throw new NullPointerException("startTime");
        }
        DTVScheduleParam param = new DTVScheduleParam();
        param.setFrequency(frequency);
        param.setServiceId(serviceId);
        param.setServiceName(serviceName);
        Calendar start = Calendar.getInstance();
        start.set(Calendar.YEAR, startTime.get(Calendar.YEAR));
        start.set(Calendar.MONTH, startTime.get(Calendar.MONTH));
        start.set(Calendar.DAY_OF_MONTH, startTime.get(Calendar.DAY_OF_MONTH));
        start.set(Calendar.HOUR_OF_DAY, startTime.get(Calendar.HOUR_OF_DAY));
        start.set(Calendar.MINUTE, startTime.get(Calendar.MINUTE));
        param.setStartTime(start);
        param.setRepeatType(startTime.getType());
        param.setEventName(eventName);
        return _SystemSupport.addDtvSchedule(TYPE_WATCH, param);
    }

    /**
     * Registers a watch or record reservation.
     *
     * @param type the reservation type
     * @param param the reservation parameters
     * @return {@code true} if registration succeeds
     * @throws InterruptedOperationException declared by the API contract
     */
    public static boolean addEntry(int type, DTVScheduleParam param) throws InterruptedOperationException {
        return _SystemSupport.addDtvSchedule(type, param);
    }
}
