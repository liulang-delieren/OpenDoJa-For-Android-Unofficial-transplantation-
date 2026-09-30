package opendoja.audio.mld.fuetrek;

import opendoja.audio.mld.*;

public final class FueTrekMLDPlaybackEngine
    implements MLDPlaybackEngine
{
    private final Sampler sampler;
    private final MLDResourceAudioEngine resourceAudioEngine;

    public FueTrekMLDPlaybackEngine(Sampler sampler, MLD mld, float sampleRate)
    {
        this.sampler = sampler;
        this.resourceAudioEngine = new FueTrekResourceAudioEngine(mld,
            sampleRate);
    }

    @Override
    public MLDTrackControlMode trackControlMode()
    {
        return MLDTrackControlMode.FUETREK;
    }

    @Override
    public void reset()
    {
        this.resourceAudioEngine.reset();
    }

    @Override
    public void handleResource(MLDPlayer player, MLDPlayerTrack track,
        MLDEvent event)
    {
        this.resourceAudioEngine.handleEvent(player.framePosition(), event);
        player.advanceTrack(track);
    }

    @Override
    public void render(float[] samples, int offset, int frames, float left,
        float right, boolean clamp, long framePosition)
    {
        this.resourceAudioEngine.render(samples, offset, frames, left, right,
            clamp, framePosition);
    }

    @Override
    public boolean hasLiveAudio(long framePosition)
    {
        return this.resourceAudioEngine.hasLiveAudio(framePosition);
    }

    @Override
    public int framesUntilSilence(long framePosition)
    {
        return this.resourceAudioEngine.framesUntilSilence(framePosition);
    }

    @Override
    public void handleExtB(MLDPlayer player, MLDPlayerTrack track,
        MLDEvent event)
    {
        if (!this.interceptsExtBEvent(event.getId()))
        {
            MLDNormalizedEventDispatcher.dispatch(player, track, event);
            return;
        }

        if (!(this.sampler instanceof MLDRawExtBHandler))
        {
            player.advanceTrack(track);
            return;
        }

        int channel = player.resolveRuntimeChannel(track,
            event.getChannelIndex(), event.getChannel());
        ((MLDRawExtBHandler)this.sampler).handleRawExtBEvent(event.getId(),
            channel, event.getParam() & 0xFF);
        player.advanceTrack(track);
    }

    private boolean interceptsExtBEvent(int eventId)
    {
        switch (eventId & 0xff)
        {
            case MLD.EVENT_MASTER_VOLUME:
            case 0xb1:
            case MLD.EVENT_X_DRUM_ENABLE:
            case MLD.EVENT_PROGRAM_CHANGE:
            case MLD.EVENT_BANK_CHANGE:
            case MLD.EVENT_VOLUME:
            case MLD.EVENT_PANPOT:
            case MLD.EVENT_PITCHBEND:
            case 0xe6:
            case MLD.EVENT_PITCHBEND_RANGE:
            case MLD.EVENT_WAVE_CHANNEL_VOLUME:
            case MLD.EVENT_WAVE_CHANNEL_PANPOT:
            case 0xea:
                return true;

            default:
                return false;
        }
    }
}
