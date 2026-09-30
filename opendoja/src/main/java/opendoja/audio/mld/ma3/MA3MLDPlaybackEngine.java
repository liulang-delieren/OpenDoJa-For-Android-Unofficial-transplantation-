package opendoja.audio.mld.ma3;

import opendoja.audio.mld.*;

public final class MA3MLDPlaybackEngine
    implements MLDPlaybackEngine
{
    public MA3MLDPlaybackEngine(Sampler sampler)
    {
    }

    @Override
    public MLDTrackControlMode trackControlMode()
    {
        return MLDTrackControlMode.GENERIC_TRACK_CONTROL;
    }

    @Override
    public void reset()
    {
    }

    @Override
    public void handleExtB(MLDPlayer player, MLDPlayerTrack track,
        MLDEvent event)
    {
        MLDNormalizedEventDispatcher.dispatch(player, track, event);
    }

    @Override
    public void handleResource(MLDPlayer player, MLDPlayerTrack track,
        MLDEvent event)
    {
        player.advanceTrack(track);
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
