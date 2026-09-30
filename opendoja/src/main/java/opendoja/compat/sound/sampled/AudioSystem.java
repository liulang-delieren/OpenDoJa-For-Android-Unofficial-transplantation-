package opendoja.compat.sound.sampled;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import opendoja.host.OpenDoJaLog;

public class AudioSystem {
    public AudioSystem() { }

    public static AudioInputStream getAudioInputStream(InputStream stream)
            throws UnsupportedAudioFileException, IOException {
        byte[] all = readAll(stream);
        WavData wav = parseWav(all);
        int frameSize = wav.format.getFrameSize();
        long frames = frameSize > 0 ? wav.pcm.length / frameSize : 0L;
        OpenDoJaLog.debug(AudioSystem.class, () -> "wav: " + wav.format
                + ", " + wav.pcm.length + " B");
        return new AudioInputStream(new ByteArrayInputStream(wav.pcm), wav.format, frames);
    }

    public static AudioInputStream getAudioInputStream(AudioFormat target, AudioInputStream source) {
        if (target == null || source == null) {
            throw new NullPointerException();
        }
        AudioFormat from = source.getFormat();
        if (from == null) {
            throw new IllegalArgumentException("source format missing");
        }
        byte[] input;
        try {
            input = readAll(source);
        } catch (IOException exception) {
            throw new IllegalArgumentException("failed to read source stream", exception);
        }
        if (target.matches(from)) {
            int frameSize = target.getFrameSize();
            long frames = frameSize > 0 ? input.length / frameSize : 0L;
            return new AudioInputStream(new ByteArrayInputStream(input), target, frames);
        }
        byte[] output = convert(input, from, target);
        int frameSize = target.getFrameSize();
        long frames = frameSize > 0 ? output.length / frameSize : 0L;
        OpenDoJaLog.debug(AudioSystem.class,
                () -> "decoded: " + from + " -> " + target + ", " + output.length + " B");
        return new AudioInputStream(new ByteArrayInputStream(output), target, frames);
    }

    public static SourceDataLine getSourceDataLine(AudioFormat p0)
            throws LineUnavailableException {
        return new AndroidSourceDataLine();
    }

    private static byte[] convert(byte[] input, AudioFormat from, AudioFormat target) {
        int srcChannels = from.getChannels();
        int dstChannels = target.getChannels();
        int srcBits = from.getSampleSizeInBits();
        int dstBits = target.getSampleSizeInBits();
        int srcRate = Math.round(from.getFrameRate());
        int dstRate = Math.round(target.getFrameRate());
        boolean supportedSource = (srcBits == 8 || srcBits == 16 || srcBits == 24)
                && (srcChannels == 1 || srcChannels == 2);
        boolean supportedTarget = (dstBits == 8 || dstBits == 16)
                && (dstChannels == 1 || dstChannels == 2);
        if (!supportedSource || !supportedTarget || srcRate != dstRate) {
            throw new IllegalArgumentException(
                    "unsupported conversion " + from + " -> " + target);
        }
        int srcFrameSize = srcChannels * (srcBits / 8);
        int dstFrameSize = dstChannels * (dstBits / 8);
        int frames = input.length / srcFrameSize;
        byte[] output = new byte[frames * dstFrameSize];
        boolean srcBigEndian = from.isBigEndian();
        boolean srcSigned = !AudioFormat.Encoding.PCM_UNSIGNED.equals(from.getEncoding());
        boolean dstBigEndian = target.isBigEndian();
        boolean dstSigned = !AudioFormat.Encoding.PCM_UNSIGNED.equals(target.getEncoding());
        for (int frame = 0; frame < frames; frame++) {
            int srcBase = frame * srcFrameSize;
            int sample0 = readSample(input, srcBase, srcBits, srcBigEndian, srcSigned);
            int sample1;
            if (srcChannels == 2) {
                sample1 = readSample(input, srcBase + srcBits / 8, srcBits, srcBigEndian, srcSigned);
            } else {
                sample1 = sample0;
            }
            int left;
            int right;
            if (srcChannels == 2 && dstChannels == 1) {
                left = right = ((sample0 + sample1) / 2);
            } else if (srcChannels == 1 && dstChannels == 2) {
                left = sample0;
                right = sample0;
            } else {
                left = sample0;
                right = sample1;
            }
            int dstBase = frame * dstFrameSize;
            writeSample(output, dstBase, left, dstBits, dstBigEndian, dstSigned);
            if (dstChannels == 2) {
                writeSample(output, dstBase + dstBits / 8, right, dstBits, dstBigEndian, dstSigned);
            }
        }
        return output;
    }

    private static int readSample(byte[] data, int offset, int bits, boolean bigEndian,
            boolean signed) {
        if (bits == 8) {
            int raw = data[offset] & 0xFF;
            if (!signed) {
                raw -= 128;
            } else {
                raw = (byte) raw;
            }
            return raw << 8;
        }
        if (bits == 16) {
            int lo;
            int hi;
            if (bigEndian) {
                hi = data[offset];
                lo = data[offset + 1] & 0xFF;
            } else {
                lo = data[offset] & 0xFF;
                hi = data[offset + 1];
            }
            return (short) ((hi << 8) | lo);
        }
        // bits == 24; scale down to the shared 16-bit sample space.
        int b0;
        int b1;
        int b2;
        if (bigEndian) {
            b2 = data[offset];
            b1 = data[offset + 1] & 0xFF;
            b0 = data[offset + 2] & 0xFF;
        } else {
            b0 = data[offset] & 0xFF;
            b1 = data[offset + 1] & 0xFF;
            b2 = data[offset + 2];
        }
        return ((b2 << 24) | (b1 << 16) | (b0 << 8)) >> 16;
    }

    private static void writeSample(byte[] data, int offset, int sample16, int bits,
            boolean bigEndian, boolean signed) {
        if (bits == 16) {
            int lo = sample16 & 0xFF;
            int hi = (sample16 >> 8) & 0xFF;
            if (bigEndian) {
                data[offset] = (byte) hi;
                data[offset + 1] = (byte) lo;
            } else {
                data[offset] = (byte) lo;
                data[offset + 1] = (byte) hi;
            }
            return;
        }
        // bits == 8
        int value = sample16 >> 8;
        if (!signed) {
            value += 128;
        }
        data[offset] = (byte) value;
    }

    private static final class WavData {
        final AudioFormat format;
        final byte[] pcm;

        WavData(AudioFormat format, byte[] pcm) {
            this.format = format;
            this.pcm = pcm;
        }
    }

    private static WavData parseWav(byte[] all) throws UnsupportedAudioFileException {
        if (all.length < 12
                || all[0] != 'R' || all[1] != 'I' || all[2] != 'F' || all[3] != 'F'
                || all[8] != 'W' || all[9] != 'A' || all[10] != 'V' || all[11] != 'E') {
            throw new UnsupportedAudioFileException();
        }
        AudioFormat format = null;
        byte[] pcm = null;
        int pos = 12;
        while (pos + 8 <= all.length) {
            String chunkId = new String(all, pos, 4, StandardCharsets.US_ASCII);
            long chunkSize = le32(all, pos + 4) & 0xFFFFFFFFL;
            int body = pos + 8;
            long available = all.length - body;
            if (chunkSize > available) {
                // Tolerate files whose final chunk size field overruns the payload.
                chunkSize = available;
            }
            if ("fmt ".equals(chunkId)) {
                format = parseFmtChunk(all, body, (int) chunkSize);
            } else if ("data".equals(chunkId)) {
                pcm = new byte[(int) chunkSize];
                System.arraycopy(all, body, pcm, 0, (int) chunkSize);
            }
            long next = body + chunkSize + (chunkSize & 1L);
            if (next <= pos || next > all.length) {
                break;
            }
            pos = (int) next;
        }
        if (format == null || pcm == null) {
            throw new UnsupportedAudioFileException();
        }
        return new WavData(format, pcm);
    }

    private static AudioFormat parseFmtChunk(byte[] all, int body, int size)
            throws UnsupportedAudioFileException {
        if (size < 16 || body + 16 > all.length) {
            throw new UnsupportedAudioFileException();
        }
        int tag = le16(all, body);
        int channels = le16(all, body + 2);
        int sampleRate = le32(all, body + 4);
        int bits = le16(all, body + 14);
        if (tag == 0xFFFE) {
            // WAVE_FORMAT_EXTENSIBLE: the real tag sits at the start of the
            // SubFormat GUID (offset 24 within the fmt chunk body).
            if (size < 40 || body + 40 > all.length) {
                throw new UnsupportedAudioFileException();
            }
            tag = le16(all, body + 24);
        }
        if (tag != 1) {
            // Only LPCM content is produced by DoJa sampled sounds; anything
            // else (ADPCM, MP3-in-WAV, ...) stays unsupported.
            throw new UnsupportedAudioFileException();
        }
        if (channels < 1 || channels > 2 || sampleRate <= 0
                || (bits != 8 && bits != 16 && bits != 24)) {
            throw new UnsupportedAudioFileException();
        }
        AudioFormat.Encoding encoding =
                bits == 8 ? AudioFormat.Encoding.PCM_UNSIGNED : AudioFormat.Encoding.PCM_SIGNED;
        int frameSize = channels * (bits / 8);
        return new AudioFormat(encoding, sampleRate, bits, channels, frameSize, sampleRate, false);
    }

    private static int le16(byte[] data, int offset) {
        return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8);
    }

    private static int le32(byte[] data, int offset) {
        return (data[offset] & 0xFF)
                | ((data[offset + 1] & 0xFF) << 8)
                | ((data[offset + 2] & 0xFF) << 16)
                | ((data[offset + 3] & 0xFF) << 24);
    }

    private static byte[] readAll(InputStream stream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(Math.max(4096, stream.available()));
        byte[] chunk = new byte[8192];
        int read;
        while ((read = stream.read(chunk)) >= 0) {
            if (read > 0) {
                buffer.write(chunk, 0, read);
            }
        }
        return buffer.toByteArray();
    }

    public static final int NOT_SPECIFIED = -1;
}
