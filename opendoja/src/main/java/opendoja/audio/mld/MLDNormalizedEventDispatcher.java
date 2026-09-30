package opendoja.audio.mld;

/**
 * Shared normalized ext-B event dispatcher used by synth playback modules that
 * follow the standard MLD event behavior.
 */
public final class MLDNormalizedEventDispatcher
{
    private MLDNormalizedEventDispatcher()
    {
    }

    public static void dispatch(MLDPlayer player, MLDPlayerTrack track,
        MLDEvent event)
    {
        switch (event.getId())
        {
            case MLD.EVENT_CHANNEL_ASSIGN:
                player.evtChannelAssign(track, event);
                return;

            case MLD.EVENT_TRACK_CONTROL:
                player.evtTrackControl(track, event);
                return;

            case MLD.EVENT_BANK_CHANGE:
                player.evtBankChange(track, event);
                return;

            case MLD.EVENT_CUEPOINT:
                player.evtCuepoint(track, event);
                return;

            case MLD.EVENT_END_OF_TRACK:
                player.evtEndOfTrack(track, event);
                return;

            case MLD.EVENT_MASTER_VOLUME:
                player.evtMasterVolume(track, event);
                return;

            case MLD.EVENT_MASTER_TUNE:
                player.evtMasterTune(track, event);
                return;

            case MLD.EVENT_PANPOT:
                player.evtPanPot(track, event);
                return;

            case MLD.EVENT_PITCHBEND:
                player.evtPitchBend(track, event);
                return;

            case MLD.EVENT_PITCHBEND_RANGE:
                player.evtPitchRange(track, event);
                return;

            case MLD.EVENT_PROGRAM_CHANGE:
                player.evtProgramChange(track, event);
                return;

            case MLD.EVENT_TIMEBASE_TEMPO:
                player.evtTimebaseTempo(track, event);
                return;

            case MLD.EVENT_VOLUME:
                player.evtVolume(track, event);
                return;

            case MLD.EVENT_WAVE_CHANNEL_PANPOT:
                player.evtPanPot(track, event);
                return;

            case MLD.EVENT_WAVE_CHANNEL_VOLUME:
                player.evtVolume(track, event);
                return;

            case MLD.EVENT_X_DRUM_ENABLE:
                player.evtDrumEnable(track, event);
                return;

            default:
                player.advanceTrack(track, track.offset + 1);
        }
    }
}
