package com.nttdocomo.device;

import com.nttdocomo.util.EventListener;

/**
 * Defines a listener for receiving events from speech feature extraction processing.
 */
public interface SpeechListener extends EventListener {
    int EVENT_GOT_FEATURE = 0;
    int EVENT_STOP = 1;
    int EVENT_ERROR = 2;
    int STOP_TRIGGER = 0;
    int STOP_TIMEOUT = 1;
    int STOP_RACE_CONDITION = 2;
    int STOP_RESET = 3;
    int STOP_UNAVAILABLE = 4;
    int ERROR_SYSTEMERROR = -1;
    int ERROR_BUFFER_OVERFLOW = -2;

    void notifyEvent(SpeechRecognizer recognizer, int event, int param);

    void notifyFeatureStored(SpeechRecognizer recognizer, SpeechAssistantInformation info);
}
