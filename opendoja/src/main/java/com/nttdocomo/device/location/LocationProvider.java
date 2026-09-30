package com.nttdocomo.device.location;

import com.nttdocomo.device.DeviceException;
import com.nttdocomo.lang.UnsupportedOperationException;

/**
 * Provides a means of accessing the function that acquires the current
 * location.
 * To access a positioning function, it is necessary to call
 * {@link #getLocationProvider()} and obtain an instance representing the
 * positioning function.
 */
public abstract class LocationProvider {
    /** Represents the capability that tracking mode (periodic positioning) is available (=0). */
    public static final int CAPABILITY_TRACKING_MODE = 0;

    /** Represents the WGS84 datum (=0). */
    public static final int DATUM_WGS84 = 0;

    /** Represents the Tokyo datum (=1). */
    public static final int DATUM_TOKYO = 1;

    /** Represents a positioning method that mainly uses GPS (=0). */
    public static final int METHOD_GPS = 0;

    /** Represents the quality-priority measurement mode (=1). */
    public static final int MODE_QUALITY_PRIORITY = 1;

    /** Represents the standard measurement mode (=0). */
    public static final int MODE_STANDARD = 0;

    /** Represents DEGREE units (=1). */
    public static final int UNIT_DEGREE = 1;

    /** Represents DMS units with 1/100-second precision (=0). */
    public static final int UNIT_DMS = 0;

    /** Represents DMS units with 1/1000-second precision (=2). */
    public static final int UNIT_DMS_2 = 2;

    private int measurementMode = MODE_STANDARD;

    LocationProvider() {
    }

    /**
     * Gets the supported positioning methods.
     * If the location-acquisition feature itself is not supported, {@code null}
     * is returned.
     *
     * @return an array containing all supported positioning methods, or
     *         {@code null} if location acquisition is not supported
     */
    public static int[] getAvailableLocationMethod() {
        return _LocationSupport.availableLocationMethods();
    }

    /**
     * Gets the list of supported positioning methods that have the specified
     * capability.
     *
     * @param capability the capability the requested methods must have
     * @return an array containing all supported methods that have the specified
     *         capability, or {@code null} if no supported method matches
     * @throws IllegalArgumentException if {@code capability} is invalid
     */
    public static int[] getAvailableLocationMethod(int capability) {
        if (capability != CAPABILITY_TRACKING_MODE) {
            throw new IllegalArgumentException("capability out of range: " + capability);
        }
        return _LocationSupport.availableLocationMethods(capability);
    }

    /**
     * Creates and returns an instance of a positioning function that uses the
     * specified method.
     *
     * @param method the positioning method
     * @return an instance of the positioning function
     * @throws UnsupportedOperationException if the positioning feature is not
     *         supported or the specified method is not supported
     */
    public static LocationProvider getLocationProvider(int method) {
        if (method != METHOD_GPS) {
            throw new UnsupportedOperationException("Unsupported location method: " + method);
        }
        if (!_LocationSupport.gpsSupported()) {
            throw new UnsupportedOperationException("Location provider is not supported by openDoJa");
        }
        return new GPSLocationProvider();
    }

    /**
     * Creates and returns an instance of a positioning function that uses the
     * default positioning method.
     *
     * @return an instance of the positioning function
     * @throws UnsupportedOperationException if the location-acquisition feature
     *         is not supported
     */
    public static LocationProvider getLocationProvider() {
        int[] methods = getAvailableLocationMethod();
        if (methods == null || methods.length == 0) {
            throw new UnsupportedOperationException("Location provider is not supported by openDoJa");
        }
        return getLocationProvider(methods[0]);
    }

    /**
     * Performs positioning.
     * This method is equivalent to calling {@link #getLocation(int)} with the
     * implementation's default timeout value.
     *
     * @return a location object
     * @throws LocationException if positioning fails
     */
    public Location getLocation() throws LocationException {
        return getLocation(0);
    }

    /**
     * Performs positioning.
     * The calling thread blocks until positioning completes.
     *
     * @param timeout the timeout value in seconds; if 0 is specified, the
     *        maximum supported timeout value is used
     * @return a location object
     * @throws LocationException if positioning fails
     */
    public abstract Location getLocation(int timeout) throws LocationException;

    /**
     * Interrupts positioning.
     * If positioning is not in progress, this method does nothing.
     */
    public abstract void interrupt();

    /**
     * Specifies the measurement mode.
     * The mode must be one of the values returned by
     * {@link #getAvailableMeasurementMode()}.
     *
     * @param mode the measurement mode
     * @throws IllegalArgumentException if {@code mode} is invalid
     */
    public void setMeasurementMode(int mode) {
        int[] availableModes = getAvailableMeasurementMode();
        if (availableModes == null) {
            throw new IllegalArgumentException("mode out of range: " + mode);
        }
        for (int availableMode : availableModes) {
            if (availableMode == mode) {
                measurementMode = mode;
                return;
            }
        }
        throw new IllegalArgumentException("mode out of range: " + mode);
    }

    /**
     * Gets the measurement mode used by this instance during positioning.
     *
     * @return the measurement mode
     */
    public int getMeasurementMode() {
        return measurementMode;
    }

    /**
     * Gets the measurement modes that can be set.
     *
     * @return an array containing the supported measurement modes
     */
    public abstract int[] getAvailableMeasurementMode();

    /**
     * Starts or stops periodic positioning.
     * This method is supported only for positioning methods for which tracking
     * mode is enabled.
     * For unsupported positioning methods, calling this method has no effect
     * and does not raise an exception.
     *
     * @param listener the listener that receives periodic positioning results,
     *        or {@code null} to stop periodic positioning
     * @param interval the positioning interval in milliseconds
     * @param threshold the time in milliseconds after which the absence of a
     *        latest positioning result is regarded as "position unavailable";
     *        {@code -1} means it is never regarded as unavailable
     * @throws IllegalArgumentException if {@code interval} is 0 or less, or if
     *         {@code threshold} is less than -1
     * @throws DeviceException if positioning is already being performed
     *         ({@link DeviceException#BUSY_RESOURCE})
     */
    public void setTrackingListener(TrackingListener listener, int interval, int threshold) {
        if (listener == null) {
            return;
        }
        if (interval <= 0) {
            throw new IllegalArgumentException("interval out of range: " + interval);
        }
        if (threshold < -1) {
            throw new IllegalArgumentException("threshold out of range: " + threshold);
        }
    }

    /**
     * Gets the minimum positioning interval that can be specified when
     * periodic positioning is performed.
     *
     * @return the minimum interval in milliseconds, or {@code -1} if tracking
     *         mode is not supported
     */
    public abstract int getMinimalInterval();
}
