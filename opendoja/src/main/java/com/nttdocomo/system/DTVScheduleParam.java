package com.nttdocomo.system;

import java.util.Calendar;

/**
 * Defines parameters for DTV schedule registration.
 */
public final class DTVScheduleParam {
    int frequency = DTVSchedule.FREQUENCY_NONE;
    int serviceId = DTVSchedule.SERVICE_ID_NONE;
    int affiliationId = DTVSchedule.AFFILIATION_ID_NONE;
    String serviceName;
    Calendar startTime;
    Calendar endTime;
    int repeatType = com.nttdocomo.util.ScheduleDate.ONETIME;
    String eventName;

    /**
     * Creates a DTV-schedule parameter object.
     */
    public DTVScheduleParam() {
    }

    /**
     * Sets the frequency.
     *
     * @param frequency the frequency
     */
    public synchronized void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    /**
     * Sets the service ID.
     *
     * @param serviceId the service ID
     */
    public synchronized void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * Sets the affiliation ID.
     *
     * @param affiliationId the affiliation ID
     */
    public synchronized void setAffiliationId(int affiliationId) {
        this.affiliationId = affiliationId;
    }

    /**
     * Sets the service name.
     *
     * @param serviceName the service name
     */
    public synchronized void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Sets the start time.
     *
     * @param startTime the start time
     */
    public synchronized void setStartTime(Calendar startTime) {
        this.startTime = startTime == null ? null : (Calendar) startTime.clone();
    }

    /**
     * Sets the end time.
     *
     * @param endTime the end time
     */
    public synchronized void setEndTime(Calendar endTime) {
        this.endTime = endTime == null ? null : (Calendar) endTime.clone();
    }

    /**
     * Sets the repeat type.
     *
     * @param repeatType the repeat type
     */
    public synchronized void setRepeatType(int repeatType) {
        this.repeatType = repeatType;
    }

    /**
     * Sets the event name.
     *
     * @param eventName the event name
     */
    public synchronized void setEventName(String eventName) {
        this.eventName = eventName;
    }

    synchronized DTVScheduleParam copy() {
        DTVScheduleParam copy = new DTVScheduleParam();
        copy.frequency = frequency;
        copy.serviceId = serviceId;
        copy.affiliationId = affiliationId;
        copy.serviceName = serviceName;
        copy.startTime = startTime == null ? null : (Calendar) startTime.clone();
        copy.endTime = endTime == null ? null : (Calendar) endTime.clone();
        copy.repeatType = repeatType;
        copy.eventName = eventName;
        return copy;
    }
}
