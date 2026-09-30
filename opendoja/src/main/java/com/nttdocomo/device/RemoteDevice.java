package com.nttdocomo.device;

import com.nttdocomo.io.BTConnection;
import com.nttdocomo.system.InterruptedOperationException;

import java.io.IOException;

/**
 * Represents an external Bluetooth device.
 */
public class RemoteDevice {
    /** Represents the power-saving SNIFF mode (=1). */
    public static final int SNIFF_MODE = 1;

    private final String address;
    private final String deviceName;
    private final String deviceClass;
    private final boolean sppAvailable;
    private boolean disposed;
    private boolean acceptanceInterrupted;
    private int openConnections;

    RemoteDevice() {
        this("", "", "", true);
    }

    RemoteDevice(String address, String deviceName, String deviceClass) {
        this(address, deviceName, deviceClass, true);
    }

    RemoteDevice(String address, String deviceName, String deviceClass, boolean sppAvailable) {
        this.address = address;
        this.deviceName = deviceName;
        this.deviceClass = deviceClass;
        this.sppAvailable = sppAvailable;
    }

    /**
     * Gets the Bluetooth address of the external device.
     *
     * @return the Bluetooth address
     * @throws DeviceException if this object has already been disposed
     */
    public String getAddress() {
        ensureAvailable();
        return address;
    }

    /**
     * Gets the name of the external device.
     *
     * @return the device name
     * @throws DeviceException if this object has already been disposed
     */
    public String getDeviceName() {
        ensureAvailable();
        return deviceName;
    }

    /**
     * Gets the class-of-device (CoD) information of the external device.
     *
     * @return the CoD information
     * @throws DeviceException if this object has already been disposed
     */
    public String getDeviceClass() {
        ensureAvailable();
        return deviceClass;
    }

    /**
     * Checks whether the profile specified by the argument is available on the
     * external device.
     *
     * @param profile the profile to check
     * @return {@code true} if the profile is available; otherwise {@code false}
     * @throws DeviceException if this object has already been disposed
     */
    public boolean isAvailable(int profile) {
        ensureAvailable();
        return profile == Bluetooth.SPP && sppAvailable;
    }

    /**
     * Connects to the external device using the specified profile.
     *
     * @param profile the profile to use
     * @return the {@link BTConnection} object
     * @throws IllegalArgumentException if {@code profile} is unsupported
     * @throws DeviceException if this object has already been disposed
     * @throws BluetoothException if the connection fails
     * @throws InterruptedOperationException if the native function terminates
     *         abnormally because of a race condition or a similar reason
     */
    public BTConnection connect(int profile) throws InterruptedOperationException {
        ensureAvailable();
        validateProfile(profile);
        if (!sppAvailable) {
            throw new BluetoothException(BluetoothException.UNKNOWN_PROFILE,
                    "The remote device does not support the requested profile");
        }
        try {
            return _BluetoothSupport.instance().openConnection(this);
        } catch (IOException exception) {
            throw new BluetoothException(BluetoothException.UNDEFINED, exception.getMessage());
        }
    }

    /**
     * Waits for a connection request from the external device using the
     * specified profile.
     *
     * @param profile the profile to use
     * @return the {@link BTConnection} object
     * @throws IllegalArgumentException if {@code profile} is unsupported
     * @throws DeviceException if this object has already been disposed or if
     *         acceptance was interrupted
     * @throws BluetoothException if the connection fails
     * @throws InterruptedOperationException if the native function terminates
     *         abnormally because of a race condition or a similar reason
     */
    public BTConnection accept(int profile) throws InterruptedOperationException {
        ensureAvailable();
        validateProfile(profile);
        if (acceptanceInterrupted) {
            acceptanceInterrupted = false;
            throw new DeviceException(DeviceException.INTERRUPTED,
                    "Bluetooth acceptance was interrupted");
        }
        if (!sppAvailable) {
            throw new BluetoothException(BluetoothException.UNKNOWN_PROFILE,
                    "The remote device does not support the requested profile");
        }
        try {
            return _BluetoothSupport.instance().openConnection(this);
        } catch (IOException exception) {
            throw new BluetoothException(BluetoothException.UNDEFINED, exception.getMessage());
        }
    }

    /**
     * Interrupts waiting for a connection request from an external device.
     * If {@link #accept(int)} is not waiting, this method has no effect.
     *
     * @throws DeviceException if this object has already been disposed
     */
    public void interruptAcceptance() {
        ensureAvailable();
        acceptanceInterrupted = true;
    }

    /**
     * Disposes this object.
     * If this method is called after the object has already been disposed,
     * nothing happens.
     */
    public void dispose() {
        disposed = true;
    }

    /**
     * Requests a transition to a power-saving state for the external device.
     *
     * @param type the target state; specify {@link #SNIFF_MODE}
     * @throws DeviceException if this object has already been disposed or if
     *         the terminal is not in a connected state
     * @throws IllegalArgumentException if {@code type} is unsupported
     */
    public synchronized void changePowerMode(int type) {
        ensureAvailable();
        if (type != SNIFF_MODE) {
            throw new IllegalArgumentException("Unsupported power mode: " + type);
        }
        if (openConnections <= 0) {
            throw new DeviceException(DeviceException.ILLEGAL_STATE,
                    "Bluetooth power mode cannot be changed because no connection is active");
        }
    }

    void ensureAvailable() {
        if (disposed) {
            throw new DeviceException(DeviceException.ILLEGAL_STATE,
                    "RemoteDevice has already been disposed");
        }
    }

    boolean isDisposed() {
        return disposed;
    }

    synchronized void connectionOpened() {
        openConnections++;
    }

    synchronized void connectionClosed() {
        if (openConnections > 0) {
            openConnections--;
        }
    }

    private static void validateProfile(int profile) {
        if (profile != Bluetooth.SPP) {
            throw new IllegalArgumentException("Unsupported Bluetooth profile: " + profile);
        }
    }
}
