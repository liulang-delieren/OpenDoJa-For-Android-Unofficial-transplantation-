package com.nttdocomo.fs;

/**
 * Defines an access right for accessing storage devices, folders, and files
 * from an i-appli application.
 */
public class DoJaAccessToken implements AccessToken {
    /** Access is allowed only when the same UIM is used (=0x01). */
    public static final int ACCESS_UIM = 0x01;

    /** Access is allowed only on the same handset model (=0x02). */
    public static final int ACCESS_PLATFORM = 0x02;

    /** Access is allowed only within the same series (=0x04). */
    public static final int ACCESS_SERIES = 0x04;

    private final int access;
    private final int share;

    DoJaAccessToken(int access, int share) {
        this.access = access;
        this.share = share;
    }

    /**
     * Gets the access identifier.
     *
     * @return the access identifier
     */
    public int getAccess() {
        return access;
    }

    /**
     * Gets the sharing identifier.
     *
     * @return the sharing identifier
     */
    public int getShare() {
        return share;
    }
}
