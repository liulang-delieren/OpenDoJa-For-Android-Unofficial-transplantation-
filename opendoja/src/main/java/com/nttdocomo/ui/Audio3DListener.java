package com.nttdocomo.ui;

import com.nttdocomo.util.EventListener;

/**
 * Defines the listener interface for 3D audio events.
 */
public interface Audio3DListener extends EventListener {
    /**
     * Called when a 3D audio event occurs.
     *
     * @param audio3D the audio object that generated the event
     * @param type the event type
     * @param param the event parameter
     */
    void audioAction(Audio3D audio3D, int type, int param);
}
