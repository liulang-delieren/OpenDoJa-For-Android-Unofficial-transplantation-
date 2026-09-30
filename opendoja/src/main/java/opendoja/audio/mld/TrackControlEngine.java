package opendoja.audio.mld;

interface TrackControlEngine
{
    TrackControlEngine NONE = new TrackControlEngine()
    {
    };

    static TrackControlEngine create(MLDTrackControlMode mode,
        int trackCount)
    {
        if (mode == MLDTrackControlMode.NONE)
            return TrackControlEngine.NONE;
        return new SharedTrackControlEngine(trackCount, mode);
    }

    default boolean handleExtB(MLDPlayer player, MLDPlayerTrack track,
        MLDEvent event)
    {
        return false;
    }

    default void reset(MLDPlayer player)
    {
    }

    default boolean restartRequested()
    {
        return false;
    }

    default void clearRestartRequest()
    {
    }

    default boolean usesRuntimeChannelMap()
    {
        return false;
    }
}

final class SharedTrackControlEngine
    implements TrackControlEngine
{
    private final TrackControlGroupState[] groups;

    private final MLDTrackControlMode mode;

    private boolean restartRequested;

    SharedTrackControlEngine(int trackCount, MLDTrackControlMode mode)
    {
        this.mode = mode;
        this.groups = new TrackControlGroupState[4];
        for (int i = 0; i < this.groups.length; i++)
            this.groups[i] = new TrackControlGroupState(trackCount);
    }

    @Override
    public boolean handleExtB(MLDPlayer player, MLDPlayerTrack track,
        MLDEvent event)
    {
        if (event.id != MLD.EVENT_TRACK_CONTROL)
            return false;

        int groupIndex = (event.param >>> 6) & 0x3;
        int mode = event.param & 0x3;
        if (groupIndex < 0 || groupIndex >= this.groups.length)
        {
            player.setTrackOffset(track, track.offset + 1);
            return true;
        }
        TrackControlGroupState group = this.groups[groupIndex];
        switch (mode)
        {
            case 0:
                this.snapshotGroup(player, group, track);
                this.advanceTrack(player, track);
                return true;
            case 1:
                this.restoreGroup(player, groupIndex, group, track, event);
                return true;
            default:
                player.setTrackOffset(track, track.offset + 1);
                return true;
        }
    }

    @Override
    public void reset(MLDPlayer player)
    {
        for (TrackControlGroupState group : this.groups)
            group.reset();
        this.restartRequested = false;
    }

    @Override
    public boolean restartRequested()
    {
        return this.restartRequested;
    }

    @Override
    public void clearRestartRequest()
    {
        this.restartRequested = false;
    }

    @Override
    public boolean usesRuntimeChannelMap()
    {
        return switch (this.mode)
        {
            case FUETREK -> true;
            case NONE, GENERIC_TRACK_CONTROL -> false;
        };
    }

    private void snapshotGroup(MLDPlayer player, TrackControlGroupState group,
        MLDPlayerTrack currentTrack)
    {
        group.snapshotTick = player.tickNow;
        int minimumDelay = player.trackControlMinimumTrackTicks();
        for (int i = 0; i < player.tracks.length; i++)
        {
            MLDPlayerTrack live = player.tracks[i];
            TrackControlSnapshot shadow = group.tracks[i];
            int delay = live.ticks;
            if (i != 0 && !live.finished && delay > minimumDelay)
                delay -= minimumDelay;
            else if (i != 0 && !live.finished)
                delay = 0;
            shadow.capture(live.offset, delay, live.finished,
                live.trackControlReplayByte, live.trackControlRawOffset,
                live.trackControlRawEndOffset);
        }
    }

    private void advanceTrack(MLDPlayer player, MLDPlayerTrack track)
    {
        int nextOffset = track.offset + 1;
        player.setTrackOffset(track, nextOffset);
        if (!track.finished)
        {
            track.ticks = track.mld.get(nextOffset).delta;
            track.trackControlSkipReschedule = true;
        }
    }

    private void restoreGroup(MLDPlayer player, int groupIndex,
        TrackControlGroupState group, MLDPlayerTrack track, MLDEvent event)
    {
        if (!group.tracks[0].valid)
        {
            player.setTrackOffset(track, track.offset + 1);
            return;
        }
        if (group.snapshotTick == player.tickNow)
        {
            for (MLDPlayerTrack live : player.tracks)
                live.trackControlReplayByte = 1;
            track.ticks = player.trackControlRetryTicks(track);
            track.trackControlSkipReschedule = true;
            this.restartRequested = true;
            return;
        }
        int maskBit = 1 << groupIndex;
        if ((group.activeMask & maskBit) == 0)
        {
            int counter = (event.param >>> 2) & 0x0f;
            group.counter = (counter == 0 ? -1 : counter);
            if (counter == 0)
                group.flag20 = true;
            group.activeMask |= maskBit;
        }
        else
            group.counter--;
        if (group.counter == 0)
        {
            group.activeMask &= ~maskBit;
            group.tracks[0].clear();
            player.setTrackOffset(track, track.offset + 1);
            return;
        }
        for (int i = 0; i < player.tracks.length; i++)
            group.tracks[i].restore(player.tracks[i]);
        this.restartRequested = true;
    }

    private static final class TrackControlGroupState
    {
        final TrackControlSnapshot[] tracks;
        long snapshotTick;
        int counter = 0;
        int activeMask = 0;
        boolean flag20;

        TrackControlGroupState(int trackCount)
        {
            this.tracks = new TrackControlSnapshot[trackCount];
            for (int i = 0; i < trackCount; i++)
                this.tracks[i] = new TrackControlSnapshot();
        }

        void reset()
        {
            this.snapshotTick = Long.MIN_VALUE;
            this.counter = 0;
            this.activeMask = 0;
            this.flag20 = false;
            for (TrackControlSnapshot track : this.tracks)
                track.clear();
        }
    }

    private static final class TrackControlSnapshot
    {
        int offset;
        int ticks;
        boolean finished;
        boolean valid;
        int replayByte;
        int rawOffset;
        int rawEndOffset;

        void capture(int offset, int ticks, boolean finished, int replayByte,
            int rawOffset, int rawEndOffset)
        {
            this.offset = offset;
            this.ticks = Math.max(0, ticks);
            this.finished = finished;
            this.replayByte = replayByte;
            this.rawOffset = rawOffset;
            this.rawEndOffset = rawEndOffset;
            this.valid = true;
        }

        void clear()
        {
            this.offset = 0;
            this.ticks = 0;
            this.finished = true;
            this.valid = false;
            this.replayByte = 0;
            this.rawOffset = 0;
            this.rawEndOffset = 0;
        }

        void restore(MLDPlayerTrack track)
        {
            track.offset = this.offset;
            track.trackControlRawOffset = this.rawOffset;
            track.trackControlRawEndOffset = this.rawEndOffset;
            track.ticks = this.ticks;
            track.finished = this.finished || track.offset >= track.mld.size();
            track.trackControlSkipReschedule = false;
            track.trackControlReplayByte = this.replayByte;
        }
    }

}
