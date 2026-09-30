package com.nttdocomo.system;

import java.time.Instant;
import java.time.ZoneId;
import java.util.TimeZone;

/**
 * Defines the base class of native message-i-appli messages.
 */
public abstract class Message implements MailConstants {
    final int type;
    final int id;
    final Instant timestamp;
    final ZoneId defaultZone;
    final String subject;
    final String body;
    final byte[] data;

    Message(int type, int id, Instant timestamp, ZoneId defaultZone, String subject, String body, byte[] data) {
        this.type = type;
        this.id = id;
        this.timestamp = timestamp;
        this.defaultZone = defaultZone == null ? ZoneId.systemDefault() : defaultZone;
        this.subject = subject == null ? "" : subject;
        this.body = body == null ? "" : body;
        this.data = _SystemSupport.copyBytes(data);
    }

    /**
     * Gets the folder type.
     *
     * @return the folder type
     */
    public int getType() {
        return type;
    }

    /**
     * Gets the message ID.
     *
     * @return the message ID
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the send/receive date and time as an ordinary string.
     *
     * @param pattern the date-time format pattern
     * @return the formatted date-time, or {@code null} if the timestamp is not available
     */
    public String getDateString(String pattern) {
        return _SystemSupport.formatInstant(timestamp, defaultZone, pattern);
    }

    /**
     * Gets the send/receive date and time using the specified time zone.
     *
     * @param pattern the date-time format pattern
     * @param zone the time zone to use, or {@code null}
     * @return the formatted date-time, or {@code null} if the timestamp is not available
     */
    public String getDateString(String pattern, TimeZone zone) {
        return _SystemSupport.formatInstant(timestamp, zone == null ? defaultZone : zone.toZoneId(), pattern);
    }

    /**
     * Gets the subject.
     *
     * @return the subject, or an empty string if it is not set
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Gets the body.
     *
     * @return the body, or an empty string if it is not set
     */
    public String getBody() {
        return body;
    }

    /**
     * Gets the message-specific binary data.
     *
     * @return a copy of the binary data, or {@code null}
     */
    public byte[] getData() {
        return _SystemSupport.copyBytes(data);
    }
}
