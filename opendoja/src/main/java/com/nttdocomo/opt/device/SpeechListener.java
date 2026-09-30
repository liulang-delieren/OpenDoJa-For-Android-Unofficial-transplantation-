package com.nttdocomo.opt.device;

import com.nttdocomo.util.EventListener;

/**
 * Defines a listener for events related to the speech-synthesis function.
 */
public interface SpeechListener extends EventListener {
    /** Event type indicating that speech completed (=0). */
    int SPEECH_COMPLETE = 0;

    /** Event type indicating that speech was canceled (=1). */
    int SPEECH_CANCEL = 1;

    /**
     * Receives an event from the speech-synthesis function.
     *
     * @param state the event type
     */
    void speechAction(int state);
}
