package com.nttdocomo.fs;

import opendoja.host.storage.DoJaStorageHost;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Defines the DoJa storage-device service.
 * Applications obtain an instance representing an access right through this
 * class, then call
 * {@code StorageDevice.getFolder(AccessToken)} to access folders and files.
 */
public class DoJaStorageService {
    /** Indicates sharing only within the same application (=0x08). */
    public static final int SHARE_APPLICATION = 0x08;

    /** Indicates sharing only within the same contents provider (=0x10). */
    public static final int SHARE_CONTENTS_PROVIDER = 0x10;

    private static final int VALID_ACCESS_MASK = DoJaAccessToken.ACCESS_UIM
            | DoJaAccessToken.ACCESS_PLATFORM
            | DoJaAccessToken.ACCESS_SERIES;

    private static final Map<Integer, DoJaAccessToken> TOKENS = new ConcurrentHashMap<>();

    private DoJaStorageService() {
    }

    /**
     * Gets an instance representing an access right.
     *
     * @param access the access identifier, specified as a bitwise OR of the
     *        {@code ACCESS_*} constants defined by {@link DoJaAccessToken}
     * @param share the sharing identifier
     * @return the access-right instance
     * @throws IllegalArgumentException if {@code access} or {@code share} is
     *         invalid
     * @throws SecurityException if the application is not permitted to use the
     *         storage API
     */
    public static DoJaAccessToken getAccessToken(int access, int share) {
        DoJaStorageHost.ensureStoragePermission();
        if ((access & ~VALID_ACCESS_MASK) != 0) {
            throw new IllegalArgumentException("Invalid access identifier: " + access);
        }
        if (share != SHARE_APPLICATION && share != SHARE_CONTENTS_PROVIDER) {
            throw new IllegalArgumentException("Invalid share identifier: " + share);
        }
        int key = (access & 0xff) | (share << 8);
        return TOKENS.computeIfAbsent(key, ignored -> new DoJaAccessToken(access, share));
    }
}
