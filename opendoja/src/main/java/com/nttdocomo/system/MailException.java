package com.nttdocomo.system;

/**
 * An exception related to sending or receiving mail.
 */
public class MailException extends Exception {
    /**
     * Creates an exception object without a detail message.
     */
    public MailException() {
    }

    /**
     * Creates an exception object with a detail message.
     *
     * @param message the detail message
     */
    public MailException(String message) {
        super(message);
    }
}
