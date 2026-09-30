package opendoja.audio.mld;

/**
 * Synth-owned renderer for top-level MLD resource audio.
 *
 * <p>"Resource audio" refers to the legacy MLD path that stores audio outside
 * of note events, using top-level chunks such as {@code ainf} / {@code adat}
 * plus live {@code 0x7f} resource control events. Unlike normal MLD note data,
 * these resources are not part of the generic score/sequencer contract; the
 * exact decode, mix, route, and spatial behavior depends on the synth family
 * that owns the format.</p>
 *
 * <p>The core MLD layer only knows that these resource events exist. Concrete
 * synth modules provide the actual playback behavior through implementations of
 * this interface.</p>
 */
public interface MLDResourceAudioEngine
{
    void reset();

    void handleEvent(long framePosition, MLDEvent event);

    void render(float[] samples, int offset, int frames, float left,
        float right, boolean clamp, long framePosition);

    boolean hasLiveAudio(long framePosition);

    int framesUntilSilence(long framePosition);
}
