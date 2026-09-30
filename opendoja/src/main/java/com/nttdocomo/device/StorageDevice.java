package com.nttdocomo.device;

import com.nttdocomo.fs.AccessToken;
import com.nttdocomo.fs.DoJaAccessToken;
import com.nttdocomo.fs.Folder;
import opendoja.host.storage.DoJaStorageHost;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Defines a storage device.
 * This class can be used to obtain status information such as whether the
 * storage device is accessible, and to obtain instances used to access actual
 * folders and files.
 */
public class StorageDevice {
    /** Capability category string representing hardware (= "hardware"). */
    public static final String CATEGORY_HARDWARE = "hardware";

    /** Capability string representing an SD Memory Card (= "SD"). */
    public static final String CAPABILITY_SD = "SD";

    /** Capability string representing a miniSD or microSD card (= "miniSD"). */
    public static final String CAPABILITY_MINISD = "miniSD";

    /** Capability category string representing the file system (= "filesystem"). */
    public static final String CATEGORY_FILESYSTEM = "filesystem";

    /** Capability string representing FAT12 (= "FAT12"). */
    public static final String CAPABILITY_FAT12 = "FAT12";

    /** Capability string representing FAT16 (= "FAT16"). */
    public static final String CAPABILITY_FAT16 = "FAT16";

    /** Capability string representing FAT32 (= "FAT32"). */
    public static final String CAPABILITY_FAT32 = "FAT32";

    /** Capability string representing FAT long-name support (= "FAT_LONG_NAME"). */
    public static final String CAPABILITY_FAT_LONG_NAME = "FAT_LONG_NAME";

    /** Capability category string representing encryption (= "encryption"). */
    public static final String CATEGORY_ENCRYPTION = "encryption";

    /** Capability string representing SD-Binding support (= "SD-Binding"). */
    public static final String CAPABILITY_SD_BINDING = "SD-Binding";

    private static final Map<String, StorageDevice> DEVICES = new ConcurrentHashMap<>();

    private final String deviceName;

    private StorageDevice(String deviceName) {
        this.deviceName = deviceName;
    }

    /**
     * Gets the storage-device instance for the specified device name.
     * The same instance is always returned for the same device name.
     *
     * @param deviceName the device name of the storage device to obtain
     * @return the storage-device instance
     * @throws NullPointerException if {@code deviceName} is {@code null}
     * @throws IllegalArgumentException if the specified storage device does not
     *         exist
     * @throws SecurityException if the application is not permitted to use the
     *         storage API
     */
    public static StorageDevice getInstance(String deviceName) {
        DoJaStorageHost.ensureStoragePermission();
        if (deviceName == null) {
            throw new NullPointerException("deviceName");
        }
        if (!DoJaStorageHost.EXTERNAL_DEVICE_NAME.equals(deviceName)) {
            throw new IllegalArgumentException("Unknown storage device: " + deviceName);
        }
        return DEVICES.computeIfAbsent(deviceName, StorageDevice::new);
    }

    /**
     * Gets the storage device name.
     *
     * @return the device name
     */
    public String getDeviceName() {
        return deviceName;
    }

    /**
     * Gets the display name of the storage device.
     *
     * @return the display name
     */
    public String getPrintName() {
        return DoJaStorageHost.PRINT_NAME;
    }

    /**
     * Gets whether the medium is removable.
     *
     * @return {@code true}
     */
    public boolean isRemovable() {
        return DoJaStorageHost.EXTERNAL_DEVICE_NAME.equals(deviceName);
    }

    /**
     * Gets whether the medium is accessible.
     *
     * @return {@code true} if the virtual storage medium is accessible
     */
    public boolean isAccessible() {
        Path root = DoJaStorageHost.deviceRoot().toAbsolutePath().normalize();
        if (Files.exists(root)) {
            return Files.isReadable(root) || Files.isWritable(root);
        }
        Path parent = root.getParent();
        return parent == null || Files.isWritable(parent);
    }

    /**
     * Gets whether the medium is readable.
     *
     * @return {@code true} if the virtual storage medium is readable
     */
    public boolean isReadable() {
        if (!isAccessible()) {
            return false;
        }
        Path root = DoJaStorageHost.deviceRoot().toAbsolutePath().normalize();
        return !Files.exists(root) || Files.isReadable(root);
    }

    /**
     * Gets whether the medium is writable.
     *
     * @return {@code true} if the virtual storage medium is writable
     */
    public boolean isWritable() {
        if (!isAccessible()) {
            return false;
        }
        Path root = DoJaStorageHost.deviceRoot().toAbsolutePath().normalize();
        if (Files.exists(root)) {
            return Files.isWritable(root);
        }
        Path parent = root.getParent();
        return parent == null || Files.isWritable(parent);
    }

    /**
     * Gets the capabilities of the medium.
     *
     * @param category the capability category to obtain, or {@code null} to
     *        obtain all categories
     * @return an array of capability strings, or {@code null} if no capability
     *         is defined for the category
     * @throws IOException if an I/O error occurs
     */
    public String[] getCapability(String category) throws IOException {
        return DoJaStorageHost.getCapabilities(category);
    }

    /**
     * Gets the identifier of the medium.
     *
     * @return the medium identifier
     * @throws IOException if an I/O error occurs
     */
    public String getMediaId() throws IOException {
        return DoJaStorageHost.mediaId();
    }

    /**
     * Gets a folder instance corresponding to the specified access right.
     * If that folder does not exist in the file system, it is created.
     *
     * @param accessToken the access right of the folder to obtain
     * @return the folder instance
     * @throws NullPointerException if {@code accessToken} is {@code null}
     * @throws IllegalArgumentException if {@code accessToken} is not a system
     *         provided access token
     * @throws IOException if the folder cannot be created or accessed
     */
    public Folder getFolder(AccessToken accessToken) throws IOException {
        if (accessToken == null) {
            throw new NullPointerException("accessToken");
        }
        if (accessToken.getClass() != DoJaAccessToken.class) {
            throw new IllegalArgumentException("Unsupported access token type: " + accessToken.getClass().getName());
        }
        DoJaAccessToken token = (DoJaAccessToken) accessToken;
        Path namespaceRoot = DoJaStorageHost.resolveNamespaceRoot(token);
        DoJaStorageHost.ensureNamespaceExists(namespaceRoot);
        return Folder.root(this, token, namespaceRoot);
    }
}
