package com.nttdocomo.util;

/**
 * An exception that indicates that a JAR-file-format error has occurred.
 * Introduced in DoJa-3.0 (505i).
 */
public class JarFormatException extends Exception {
    /**
     * Creates a {@code JarFormatException} without a detail message.
     */
    public JarFormatException() {
    }

    /**
     * Creates a {@code JarFormatException} with a detail message.
     *
     * @param message the detail message
     */
    public JarFormatException(String message) {
        super(message);
    }
}
