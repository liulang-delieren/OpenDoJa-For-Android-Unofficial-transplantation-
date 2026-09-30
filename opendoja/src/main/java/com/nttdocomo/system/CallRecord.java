package com.nttdocomo.system;

import com.nttdocomo.lang.XString;

import java.time.Instant;
import java.time.ZoneId;
import java.util.TimeZone;

/**
 * Defines a native incoming or outgoing call-history entry.
 */
public final class CallRecord {
    /** Indicates the incoming-call history type (=0). */
    public static final int CALL_IN = 0;

    /** Indicates the outgoing-call history type (=1). */
    public static final int CALL_OUT = 1;

    /** Indicates ordinary voice calling (=0). */
    public static final int TYPE_TEL = 0;

    /** Indicates video calling (=1). */
    public static final int TYPE_TEL_AV = 1;

    /** Indicates 64K data communication (=2). */
    public static final int TYPE_DATA_CONNECTION = 2;

    /** Indicates PPP packet communication (=3). */
    public static final int TYPE_PPP_PACKET_CONNECTION = 3;

    /** Indicates another call or communication kind (=4). */
    public static final int TYPE_OTHER = 4;

    final Instant timestamp;
    final ZoneId zone;
    final XString phoneNumber;
    final int[][] phoneBookIds;
    final Boolean succeeded;
    final Integer telType;

    CallRecord(Instant timestamp, ZoneId zone, XString phoneNumber, int[][] phoneBookIds,
               Boolean succeeded, Integer telType) {
        this.timestamp = timestamp;
        this.zone = zone == null ? ZoneId.systemDefault() : zone;
        this.phoneNumber = phoneNumber;
        this.phoneBookIds = _SystemSupport.copyIntMatrix(phoneBookIds);
        this.succeeded = succeeded;
        this.telType = telType;
    }

    /**
     * Gets the latest remaining call-history entry of the specified type.
     *
     * @param type the history type
     * @return the latest call-history entry, or {@code null}
     */
    public static CallRecord getLastRecord(int type) {
        return _SystemSupport.getLastCallRecord(type);
    }

    /**
     * Gets the call date and time as an {@link XString}.
     *
     * @param pattern the date-time format pattern
     * @return the formatted date-time, or {@code null}
     */
    public XString getDateString(String pattern) {
        String value = _SystemSupport.formatInstant(timestamp, zone, pattern);
        return value == null ? null : new XString(value);
    }

    /**
     * Gets the call date and time using the specified time zone.
     *
     * @param pattern the date-time format pattern
     * @param zone the time zone to use, or {@code null}
     * @return the formatted date-time, or {@code null}
     */
    public XString getDateString(String pattern, TimeZone zone) {
        String value = _SystemSupport.formatInstant(timestamp, zone == null ? this.zone : zone.toZoneId(), pattern);
        return value == null ? null : new XString(value);
    }

    /**
     * Gets matching phone-book IDs.
     *
     * @return a copy of the matching IDs, or {@code null}
     */
    public final int[][] getPhoneBookID() {
        return _SystemSupport.copyIntMatrix(phoneBookIds);
    }

    /**
     * Gets the other party's phone number.
     *
     * @return the phone number, or {@code null}
     */
    public final XString getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Gets whether the connection succeeded.
     *
     * @return {@link Boolean#TRUE}, {@link Boolean#FALSE}, or {@code null}
     */
    public final Boolean isSucceeded() {
        return succeeded;
    }

    /**
     * Gets the call kind.
     *
     * @return the call kind, or {@code null}
     */
    public Integer getTelType() {
        return telType;
    }
}
