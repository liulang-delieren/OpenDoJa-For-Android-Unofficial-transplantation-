package opendoja.audio.mld;

final class BasicMLDPlaybackEngine
    extends AbstractMLDPlaybackEngine
{
    BasicMLDPlaybackEngine(Sampler sampler)
    {
        super(sampler);
    }

    @Override
    public void handleExtB(MLDPlayer player, MLDPlayerTrack track,
        MLDEvent event)
    {
        MLDNormalizedEventDispatcher.dispatch(player, track, event);
    }
}
