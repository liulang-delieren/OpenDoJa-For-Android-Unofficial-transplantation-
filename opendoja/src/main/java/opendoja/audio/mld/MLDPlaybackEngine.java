package opendoja.audio.mld;

/**
 * Synth-owned MLD playback behavior.
 *
 * The core {@link MLDPlayer} remains responsible for parsing, timing, and
 * normalized sequencing. Synth families own everything that depends on a
 * concrete playback backend: ext-B quirks, track-control policy selection, and
 * optional top-level resource-audio rendering.
 */
public interface MLDPlaybackEngine
{
    /**
     * The scheduler model this synth wants for raw track-control opcodes.
     *
     * @return The synth-selected track-control mode.
     */
    MLDTrackControlMode trackControlMode();

    /**
     * Reset synth-owned MLD playback state.
     */
    void reset();

    /**
     * Handle an ext-B event.
     *
     * @param player The active player.
     * @param track The source track.
     * @param event The parsed event.
     */
    void handleExtB(MLDPlayer player, MLDPlayerTrack track, MLDEvent event);

    /**
     * Handle a top-level resource event.
     *
     * @param player The active player.
     * @param track The source track.
     * @param event The parsed event.
     */
    void handleResource(MLDPlayer player, MLDPlayerTrack track, MLDEvent event);

    /**
     * Render synth-owned overlay/resource audio.
     */
    void render(float[] samples, int offset, int frames, float left,
        float right, boolean clamp, long framePosition);

    /**
     * Whether synth-owned overlay/resource audio is still active.
     *
     * @param framePosition The current player frame position.
     * @return {@code true} if synth-owned audio is still live.
     */
    boolean hasLiveAudio(long framePosition);

    /**
     * The number of frames until synth-owned overlay/resource audio becomes
     * silent.
     *
     * @param framePosition The current player frame position.
     * @return The number of remaining frames, or {@code 0}.
     */
    int framesUntilSilence(long framePosition);
}
