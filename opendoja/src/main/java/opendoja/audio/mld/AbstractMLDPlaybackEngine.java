package opendoja.audio.mld;

abstract class AbstractMLDPlaybackEngine
    implements MLDPlaybackEngine
{
    protected final Sampler sampler;

    AbstractMLDPlaybackEngine(Sampler sampler)
    {
        this.sampler = sampler;
    }

    @Override
    public MLDTrackControlMode trackControlMode()
    {
        return MLDTrackControlMode.NONE;
    }

    @Override
    public void reset()
    {
    }

    @Override
    public void handleResource(MLDPlayer player, MLDPlayerTrack track,
        MLDEvent event)
    {
        player.setTrackOffset(track, track.offset + 1);
    }

    @Override
    public void render(float[] samples, int offset, int frames, float left,
        float right, boolean clamp, long framePosition)
    {
    }

    @Override
    public boolean hasLiveAudio(long framePosition)
    {
        return false;
    }

    @Override
    public int framesUntilSilence(long framePosition)
    {
        return 0;
    }
}
