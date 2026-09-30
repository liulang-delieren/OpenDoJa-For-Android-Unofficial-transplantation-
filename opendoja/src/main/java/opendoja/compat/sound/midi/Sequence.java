package opendoja.compat.sound.midi;

public class Sequence {
    public Sequence() { }
    public long getTickLength()  { return 0L; }
    public long getMicrosecondLength()  { return 0L; }
    public opendoja.compat.sound.midi.Track[] getTracks()  { return null; }
    public static final float PPQ = 0.0f;
    public static final float SMPTE_24 = 24.0f;
    public static final float SMPTE_25 = 25.0f;
    public static final float SMPTE_30DROP = 29.97f;
    public static final float SMPTE_30 = 30.0f;
}

