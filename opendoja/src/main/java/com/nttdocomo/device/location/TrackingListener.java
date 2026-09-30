package com.nttdocomo.device.location;

import com.nttdocomo.util.EventListener;

/**
 * Called back periodically while periodic positioning has been started.
 */
public interface TrackingListener extends EventListener {
    /**
     * Called periodically when periodic positioning has been started.
     *
     * @param src the positioning-function object on which this listener is set
     * @param result the latest positioning result, or {@code null} if
     *        positioning is unavailable
     * @param cause the exception that caused positioning failure when
     *        {@code result} is {@code null}; when {@code result} is not
     *        {@code null}, {@code cause} is {@code null}
     */
    void locationReceived(LocationProvider src, Location result, Exception cause);
}
