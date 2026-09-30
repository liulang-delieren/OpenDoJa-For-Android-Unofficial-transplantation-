package com.nttdocomo.system;

/**
 * Defines the parameter supplied when an application is launched from DTV.
 */
public final class DTVParameter {
    private final int serviceId;
    private final long time;

    DTVParameter(int serviceId, long time) {
        this.serviceId = serviceId;
        this.time = time;
    }

    /**
     * Gets the last DTV launch parameter.
     *
     * @return the last DTV launch parameter, or {@code null}
     */
    public static DTVParameter getLastParameter() {
        return _SystemSupport.getLastDtvParameter();
    }

    /**
     * Gets the service ID.
     *
     * @return the service ID
     */
    public int getServiceId() {
        return serviceId;
    }

    /**
     * Gets the launch time in milliseconds.
     *
     * @return the launch time in milliseconds
     */
    public long getTime() {
        return time;
    }
}
