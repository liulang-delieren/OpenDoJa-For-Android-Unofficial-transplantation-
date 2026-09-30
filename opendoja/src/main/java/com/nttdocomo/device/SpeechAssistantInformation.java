package com.nttdocomo.device;

/**
 * Represents auxiliary information for speech-recognition features.
 */
public class SpeechAssistantInformation {
    private final int level;
    private final int signalToNoiseRatio;
    private final int voiceActivity;

    SpeechAssistantInformation(int level) {
        this(level, -1, -1);
    }

    private SpeechAssistantInformation() {
        this(-1, -1, -1);
    }

    SpeechAssistantInformation(int level, int signalToNoiseRatio, int voiceActivity) {
        this.level = level;
        this.signalToNoiseRatio = signalToNoiseRatio;
        this.voiceActivity = voiceActivity;
    }

    /**
     * Gets the audio input level.
     *
     * @return the input level
     */
    public int getLevel() {
        return level;
    }

    /**
     * Gets the average S/N ratio from the start of speech until now.
     *
     * @return the S/N ratio
     */
    public int getSignalToNoiseRatio() {
        return signalToNoiseRatio;
    }

    /**
     * Gets information indicating whether voice is present.
     *
     * @return 0 for silence, 1 for voice, or -1 when unavailable
     */
    public int getVoiceActivity() {
        return voiceActivity;
    }
}
