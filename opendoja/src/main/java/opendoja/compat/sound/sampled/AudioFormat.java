package opendoja.compat.sound.sampled;

public class AudioFormat {
    public static final int NOT_SPECIFIED = -1;
    private static final float NOT_SPECIFIED_RATE = -1.0f;

    public static class Encoding {
        public static final Encoding PCM_SIGNED = new Encoding("PCM_SIGNED");
        public static final Encoding PCM_UNSIGNED = new Encoding("PCM_UNSIGNED");
        public static final Encoding PCM_FLOAT = new Encoding("PCM_FLOAT");
        public static final Encoding ALAW = new Encoding("ALAW");
        public static final Encoding ULAW = new Encoding("ULAW");

        private final String name;

        public Encoding() {
            this("NOT_SPECIFIED");
        }

        protected Encoding(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Encoding other)) {
                return false;
            }
            return name.equals(other.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private final Encoding encoding;
    private final float sampleRate;
    private final int sampleSizeInBits;
    private final int channels;
    private final float frameRate;
    private final int frameSize;
    private final boolean bigEndian;

    public AudioFormat() {
        this(Encoding.PCM_SIGNED, NOT_SPECIFIED_RATE, NOT_SPECIFIED, NOT_SPECIFIED,
                NOT_SPECIFIED, NOT_SPECIFIED_RATE, false);
    }

    public AudioFormat(float sampleRate, int sampleSizeInBits, int channels,
            boolean signed, boolean bigEndian) {
        this(signed ? Encoding.PCM_SIGNED : Encoding.PCM_UNSIGNED,
                sampleRate,
                sampleSizeInBits,
                channels,
                (sampleSizeInBits == 8 || sampleSizeInBits == 16 || sampleSizeInBits == 24
                        || sampleSizeInBits == 32)
                        ? channels * sampleSizeInBits / 8
                        : NOT_SPECIFIED,
                sampleRate,
                bigEndian);
    }

    public AudioFormat(Encoding encoding, float sampleRate, int sampleSizeInBits,
            int channels, int frameSize, float frameRate, boolean bigEndian) {
        this.encoding = encoding;
        this.sampleRate = sampleRate;
        this.sampleSizeInBits = sampleSizeInBits;
        this.channels = channels;
        this.frameSize = frameSize;
        this.frameRate = frameRate;
        this.bigEndian = bigEndian;
    }

    public Encoding getEncoding() {
        return encoding;
    }

    public float getSampleRate() {
        return sampleRate;
    }

    public int getSampleSizeInBits() {
        return sampleSizeInBits;
    }

    public int getChannels() {
        return channels;
    }

    public float getFrameRate() {
        return frameRate;
    }

    public int getFrameSize() {
        return frameSize;
    }

    public boolean isBigEndian() {
        return bigEndian;
    }

    public boolean matches(AudioFormat format) {
        if (format == null) {
            return false;
        }
        boolean encodingMatches = encoding == null
                ? format.encoding == null
                : encoding.equals(format.encoding);
        return encodingMatches
                && sampleRate == format.sampleRate
                && sampleSizeInBits == format.sampleSizeInBits
                && channels == format.channels
                && frameRate == format.frameRate
                && frameSize == format.frameSize
                && bigEndian == format.bigEndian;
    }

    @Override
    public String toString() {
        if (frameSize == NOT_SPECIFIED) {
            return "not specified";
        }
        StringBuilder builder = new StringBuilder();
        if (encoding != null) {
            builder.append(encoding.toString()).append(' ');
        }
        builder.append(sampleSizeInBits).append("-bit");
        builder.append(bigEndian ? " big-endian" : " little-endian");
        builder.append(' ');
        builder.append(channels == 1 ? "mono" : channels + " channels");
        builder.append(' ');
        builder.append((int) sampleRate);
        builder.append(" Hz");
        return builder.toString();
    }
}
