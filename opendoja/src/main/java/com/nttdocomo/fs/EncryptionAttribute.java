package com.nttdocomo.fs;

/**
 * Defines a file attribute that represents encryption parameters.
 * This attribute can be specified when a file is created in order to switch
 * encryption on or off.
 */
public class EncryptionAttribute implements FileAttribute {
    private boolean encryption = true;

    /**
     * Creates a file-attribute instance representing encryption parameters.
     * Encryption is on by default.
     */
    public EncryptionAttribute() {
    }

    /**
     * Sets whether encryption is enabled.
     *
     * @param encryption {@code true} to enable encryption, {@code false} to
     *        disable it
     */
    public void setEncryption(boolean encryption) {
        this.encryption = encryption;
    }

    boolean isEncryptionEnabled() {
        return encryption;
    }
}
