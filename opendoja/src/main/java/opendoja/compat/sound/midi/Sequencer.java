package opendoja.compat.sound.midi;

public interface Sequencer extends opendoja.compat.sound.midi.MidiDevice {
    default void open()  throws opendoja.compat.sound.midi.MidiUnavailableException {  }
    default boolean addMetaEventListener(opendoja.compat.sound.midi.MetaEventListener p0)  { return false; }
    default void setSequence(opendoja.compat.sound.midi.Sequence p0)  throws opendoja.compat.sound.midi.InvalidMidiDataException {  }
    default void setLoopCount(int p0)  {  }
    default long getMicrosecondLength()  { return 0L; }
    default void setMicrosecondPosition(long p0)  {  }
    default void setTempoFactor(float p0)  {  }
    default void start()  {  }
    default long getTickPosition()  { return 0L; }
    default void stop()  {  }
    default void setTickPosition(long p0)  {  }
    default opendoja.compat.sound.midi.Sequence getSequence()  { return null; }
    default long getMicrosecondPosition()  { return 0L; }
    default void close()  {  }
    public static final int LOOP_CONTINUOUSLY = -1;
}

