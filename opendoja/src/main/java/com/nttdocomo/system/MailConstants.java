package com.nttdocomo.system;

/**
 * Defines constants related to mail.
 */
public interface MailConstants {
    /** Requests the full address (=0). */
    int ADDRESS_FULL = 0;

    /** Requests the part before the first {@code '@'} (=1). */
    int ADDRESS_USER = 1;

    /** Requests the part after the first {@code '@'} (=2). */
    int ADDRESS_DOMAIN = 2;

    /** Indicates the received folder (=0). */
    int RECEIVED = 0;

    /** Indicates the sent folder (=1). */
    int SENT = 1;

    /** Indicates the unsent folder (=2). */
    int UNSENT = 2;
}
