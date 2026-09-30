package com.nttdocomo.device;

/**
 * Represents one recognition candidate when the recognition-result type is N-BEST.
 */
public class SpeechResultNBEST implements SpeechResult {
    private final String utterance;
    private final String words;
    private final int score;

    private SpeechResultNBEST() {
        this("", null, 1);
    }

    SpeechResultNBEST(String utterance, String words, int score) {
        this.utterance = utterance;
        this.words = words;
        this.score = score;
    }

    /**
     * Gets the recognition result in kanji/kana mixed form.
     *
     * @return the utterance
     */
    public String getUtterance() {
        return utterance;
    }

    /**
     * Gets the recognition result including furigana information and bunsetsu delimiters.
     *
     * @return the result including furigana information, or {@code null} if unavailable
     */
    public String getWords() {
        return words;
    }

    /**
     * Gets the confidence score of the recognition result.
     *
     * @return the confidence score
     */
    public int getScore() {
        return score;
    }
}
