package opendoja.compat.sound.sampled;

import android.media.AudioTrack;
import opendoja.host.OpenDoJaLog;

/**
 * {@link SourceDataLine} backed by {@link android.media.AudioTrack} in streaming mode.
 * The blocking behavior of {@link AudioTrack#write(byte[], int, int)} provides the
 * pacing that javax.sound.sampled callers expect from a source data line.
 */
public final class AndroidSourceDataLine implements SourceDataLine {
    private static final int WRITE_RETRIES_PER_FRAME = 64;

    private final Object lock = new Object();

    private AudioTrack track;
    private int frameSize = 4;
    private long writtenBytes;

    @Override
    public void open(AudioFormat format, int requestedBufferBytes) throws LineUnavailableException {
        if (format == null) {
            throw new LineUnavailableException();
        }
        int sampleRate = Math.round(format.getFrameRate());
        int channels = format.getChannels();
        int sampleSize = format.getSampleSizeInBits();
        boolean bigEndian = format.isBigEndian();
        if (sampleRate < 8000 || sampleRate > 192000
                || (channels != 1 && channels != 2)
                || sampleSize != 16
                || bigEndian) {
            // Only PCM signed 16-bit little-endian maps onto AudioTrack directly.
            // Callers normalize their formats before opening, so reaching here means
            // the host asked for something we cannot render.
            OpenDoJaLog.warn(AndroidSourceDataLine.class,
                    "unsupported line format: " + format);
            throw new LineUnavailableException();
        }
        int channelMask = channels == 2
                ? android.media.AudioFormat.CHANNEL_OUT_STEREO
                : android.media.AudioFormat.CHANNEL_OUT_MONO;
        int minBufferBytes = AudioTrack.getMinBufferSize(sampleRate, channelMask,
                android.media.AudioFormat.ENCODING_PCM_16BIT);
        if (minBufferBytes <= 0) {
            OpenDoJaLog.warn(AndroidSourceDataLine.class,
                    "AudioTrack rejected rate " + sampleRate + " (" + minBufferBytes + ")");
            throw new LineUnavailableException();
        }
        int bufferBytes = Math.max(requestedBufferBytes, minBufferBytes);
        AudioTrack candidate = new AudioTrack.Builder()
                .setAudioAttributes(new android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                .setAudioFormat(new android.media.AudioFormat.Builder()
                        .setSampleRate(sampleRate)
                        .setChannelMask(channelMask)
                        .setEncoding(android.media.AudioFormat.ENCODING_PCM_16BIT)
                        .build())
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setBufferSizeInBytes(bufferBytes)
                .build();
        if (candidate.getState() != AudioTrack.STATE_INITIALIZED) {
            candidate.release();
            OpenDoJaLog.warn(AndroidSourceDataLine.class,
                    "AudioTrack failed to initialize at " + sampleRate + " Hz");
            throw new LineUnavailableException();
        }
        synchronized (lock) {
            releaseTrackLocked();
            track = candidate;
            frameSize = channels * (sampleSize / 8);
            writtenBytes = 0L;
        }
        OpenDoJaLog.info(AndroidSourceDataLine.class,
                () -> "line open: " + sampleRate + " Hz, " + channels + " ch, buffer "
                        + bufferBytes + " B (requested " + requestedBufferBytes + " B)");
    }

    @Override
    public void start() {
        AudioTrack current;
        synchronized (lock) {
            current = track;
        }
        if (current != null && current.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
            current.play();
        }
    }

    @Override
    public void stop() {
        AudioTrack current;
        synchronized (lock) {
            current = track;
        }
        if (current != null && current.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            current.stop();
        }
    }

    @Override
    public void flush() {
        AudioTrack current;
        synchronized (lock) {
            current = track;
            if (current != null) {
                // Buffered-but-unplayed frames are discarded; reset the write
                // accounting so drain() waits on the frames queued after the flush.
                writtenBytes = 0L;
            }
        }
        if (current != null && current.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
            current.flush();
        }
    }

    @Override
    public int write(byte[] data, int offset, int length) {
        if (data == null) {
            throw new NullPointerException("data");
        }
        if (offset < 0 || length < 0 || offset > data.length - length) {
            throw new IndexOutOfBoundsException();
        }
        AudioTrack current;
        synchronized (lock) {
            current = track;
        }
        if (current == null) {
            throw new IllegalStateException("line is not open");
        }
        int total = 0;
        int stallBudget = length / Math.max(1, frameSize) * WRITE_RETRIES_PER_FRAME;
        while (total < length) {
            int written = current.write(data, offset + total, length - total);
            if (written > 0) {
                total += written;
                stallBudget = length / Math.max(1, frameSize) * WRITE_RETRIES_PER_FRAME;
                synchronized (lock) {
                    if (track == current) {
                        writtenBytes += written;
                    }
                }
                continue;
            }
            if (written == 0) {
                if (stallBudget-- <= 0) {
                    throw new IllegalStateException("AudioTrack write stalled");
                }
                try {
                    Thread.sleep(1L);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("AudioTrack write interrupted", interrupted);
                }
                continue;
            }
            // Negative values are AudioTrack error codes; MLD playback treats
            // IllegalArgumentException as the signal to fall back to its silent
            // clock, so surface the failure that way.
            throw new IllegalArgumentException("AudioTrack write failed: " + written);
        }
        return total;
    }

    @Override
    public long getLongFramePosition() {
        AudioTrack current;
        synchronized (lock) {
            current = track;
        }
        if (current == null) {
            return 0L;
        }
        return current.getPlaybackHeadPosition() & 0xFFFFFFFFL;
    }

    @Override
    public void drain() {
        long deadline = System.nanoTime() + 10_000_000_000L;
        while (System.nanoTime() < deadline) {
            long queuedBytes;
            synchronized (lock) {
                if (track == null) {
                    return;
                }
                long playedBytes =
                        (track.getPlaybackHeadPosition() & 0xFFFFFFFFL) * frameSize;
                queuedBytes = writtenBytes - playedBytes;
            }
            if (queuedBytes <= 0L) {
                return;
            }
            try {
                Thread.sleep(5L);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    @Override
    public void close() {
        synchronized (lock) {
            releaseTrackLocked();
        }
    }

    private void releaseTrackLocked() {
        if (track == null) {
            return;
        }
        try {
            if (track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                track.stop();
            }
        } catch (IllegalStateException ignored) {
            // The track may already have been stopped by the caller.
        }
        long written = writtenBytes;
        try {
            track.flush();
        } catch (IllegalStateException ignored) {
            // Flushing is best-effort during teardown.
        }
        track.release();
        track = null;
        if (written > 0L) {
            OpenDoJaLog.info(AndroidSourceDataLine.class,
                    () -> "line close: wrote " + written + " B");
        }
    }
}
