package com.nttdocomo.device;

import com.nttdocomo.lang.UnsupportedOperationException;
import com.nttdocomo.system.InterruptedOperationException;

/**
 * Represents the terminal's Bluetooth function.
 */
public class Bluetooth {
    /** Represents the Serial Port Profile (=0). */
    public static final int SPP = 0;

    private int inquiryTimeout = 5;
    private boolean detachmentMode = true;

    Bluetooth() {
    }

    /**
     * Gets an instance representing the terminal's Bluetooth function.
     * Only trusted i-appli applications that are permitted to use the
     * Bluetooth API can call this method.
     * Some terminals do not support Bluetooth; in that case an exception is
     * thrown.
     *
     * @return the Bluetooth-function instance
     * @throws UnsupportedOperationException if the terminal does not support
     *         Bluetooth
     */
    public static Bluetooth getInstance() {
        if (!_BluetoothSupport.instance().isSupported()) {
            throw new UnsupportedOperationException("Bluetooth is not supported by openDoJa");
        }
        return _BluetoothSupport.bluetooth();
    }

    /**
     * Waits for a connection request from an external device.
     * If the user selects a connection with an external device, a
     * {@link RemoteDevice} object is created.
     *
     * @return a {@link RemoteDevice} object, or {@code null} if the user
     *         cancels the processing
     * @throws InterruptedOperationException if the native function terminates
     *         abnormally because of a race condition or a similar reason
     */
    public synchronized RemoteDevice scan() throws InterruptedOperationException {
        return _BluetoothSupport.instance().scan();
    }

    /**
     * Lets the user select an external device from already-registered devices.
     *
     * @return a {@link RemoteDevice} object, or {@code null} if the user
     *         cancels device selection
     * @throws InterruptedOperationException if the native function terminates
     *         abnormally because of a race condition or a similar reason
     */
    public synchronized RemoteDevice selectDevice() throws InterruptedOperationException {
        return _BluetoothSupport.instance().selectDevice();
    }

    /**
     * Searches for surrounding external devices and lets the user select one.
     *
     * @return a {@link RemoteDevice} object, or {@code null} if the user
     *         cancels device search or selection
     * @throws InterruptedOperationException if the native function terminates
     *         abnormally because of a race condition or a similar reason
     */
    public synchronized RemoteDevice searchAndSelectDevice() throws InterruptedOperationException {
        return _BluetoothSupport.instance().searchAndSelectDevice();
    }

    /**
     * Gets the number of external devices held in the native list of connected
     * devices.
     * This method returns the number of already-registered external devices.
     *
     * @return the number of already-registered external devices held in the
     *         native list of connected devices
     */
    public int getDiscoveredDevice() {
        return _BluetoothSupport.instance().getDiscoveredDeviceCount();
    }

    /**
     * Sets the Inquiry execution time in seconds.
     * The configurable search time range is 1 to 20 seconds.
     *
     * @param time the search time in seconds
     * @throws IllegalArgumentException if {@code time} is 0 or less, or is 21
     *         or greater
     */
    public void setInquiryTimeout(int time) {
        if (time <= 0 || time >= 21) {
            throw new IllegalArgumentException("time out of range: " + time);
        }
        inquiryTimeout = time;
    }

    /**
     * Gets the Inquiry execution time in seconds.
     *
     * @return the search time in seconds
     */
    public int getInquiryTimeout() {
        return inquiryTimeout;
    }

    /**
     * Gets whether the i-appli application can connect to an external device
     * using the specified profile.
     *
     * @param type the profile to use
     * @return {@code true} if connection is possible with the specified
     *         profile, otherwise {@code false}
     */
    public boolean isConnectable(int type) {
        return type == SPP;
    }

    /**
     * Turns off Bluetooth power.
     */
    public synchronized void turnOff() {
        _BluetoothSupport.instance().turnOff();
    }

    /**
     * Sets whether the ACL link is disconnected after device registration
     * processing.
     *
     * @param flag {@code true} to disconnect, {@code false} to continue
     *        the connection
     */
    public synchronized void setDetachmentMode(boolean flag) {
        detachmentMode = flag;
    }

    /**
     * Gets whether the ACL link is disconnected after device registration
     * processing.
     *
     * @return {@code true} if the connection is disconnected, otherwise
     *         {@code false}
     */
    public synchronized boolean isDetachmentMode() {
        return detachmentMode;
    }
}
