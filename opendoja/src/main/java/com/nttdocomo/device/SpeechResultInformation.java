package com.nttdocomo.device;

/**
 * Represents the status of speech-recognition results.
 * This class makes it possible to obtain the status code, result type, and recognition results.
 */
public class SpeechResultInformation {
    /** Constant value that represents a status code meaning recognition succeeded normally (=0). */
    public static final int STATUS_CODE_SUCCESS = 0;
    /** Constant value that represents a status code meaning an unspecified warning (=100). */
    public static final int STATUS_CODE_WARNING = 100;
    /** Constant value that represents a status code meaning a warning caused by excessive noise (=101). */
    public static final int STATUS_CODE_WARNING_NOISE = 101;
    /** Constant value that represents a status code meaning a warning caused by a small voice (=102). */
    public static final int STATUS_CODE_WARNING_LITTLE_VOICE = 102;
    /** Constant value that represents a status code meaning a warning caused by a loud voice (=103). */
    public static final int STATUS_CODE_WARNING_BIG_VOICE = 103;
    /** Constant value that represents a status code meaning a warning caused by speaking too fast (=104). */
    public static final int STATUS_CODE_WARNING_FAST_SPEAKING = 104;
    /** Constant value that represents a status code meaning a warning caused by speaking too slowly (=105). */
    public static final int STATUS_CODE_WARNING_SLOW_SPEAKING = 105;
    /** Constant value that represents a status code meaning an unspecified error (=200). */
    public static final int STATUS_CODE_ERROR = 200;
    /** Constant value that represents a status code meaning an error caused by no speech input (=201). */
    public static final int STATUS_CODE_ERROR_NOSOUND = 201;
    /** Constant value that represents a status code meaning an error caused by excessive noise (=202). */
    public static final int STATUS_CODE_ERROR_NOISE = 202;
    /** Constant value that represents a status code meaning an error caused by a small voice (=203). */
    public static final int STATUS_CODE_ERROR_LITTLE_VOICE = 203;
    /** Constant value that represents a status code meaning an error caused by a loud voice (=204). */
    public static final int STATUS_CODE_ERROR_BIG_VOICE = 204;
    /** Constant value that represents a status code meaning an error caused by speaking too fast (=205). */
    public static final int STATUS_CODE_ERROR_FAST_SPEAKING = 205;
    /** Constant value that represents a status code meaning an error caused by speaking too slowly (=206). */
    public static final int STATUS_CODE_ERROR_SLOW_SPEAKING = 206;
    /** Constant value that represents a status code meaning a timeout error (=280). */
    public static final int STATUS_CODE_ERROR_TIMEOUT = 280;
    /** Constant value that represents a status code meaning there is no recognition result (=281). */
    public static final int STATUS_CODE_ERROR_NORESULT = 281;
    /** Constant value that represents a result type meaning there is no recognition result (=0). */
    public static final int TYPE_NONE = 0;
    /** Constant value that represents a result type meaning N-BEST (=1). */
    public static final int TYPE_NBEST = 1;

    private final int statusCode;
    private final int type;
    private final SpeechResult[] result;

    SpeechResultInformation(int statusCode, int type, SpeechResult[] result) {
        this.statusCode = statusCode;
        this.type = type;
        this.result = result == null ? null : result.clone();
    }

    private SpeechResultInformation() {
        this(STATUS_CODE_ERROR_NORESULT, TYPE_NONE, null);
    }

    /**
     * Gets the recognition-result status code.
     *
     * @return the recognition-result status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Gets the result type of the recognition result.
     * If there is no recognition result, {@link #TYPE_NONE} is returned.
     *
     * @return the result type
     */
    public int getType() {
        return type;
    }

    /**
     * Gets the recognition results.
     * If there are recognition results, an array of {@link SpeechResult} objects is returned.
     * If there are no recognition results, {@code null} is returned.
     *
     * @return the recognition results, or {@code null} if there are none
     */
    public SpeechResult[] getResult() {
        return result == null ? null : result.clone();
    }
}
