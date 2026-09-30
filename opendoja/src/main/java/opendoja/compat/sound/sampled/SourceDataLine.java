package opendoja.compat.sound.sampled;

public interface SourceDataLine extends opendoja.compat.sound.sampled.DataLine {
    default void stop()  {  }
    default void start()  {  }
    default void flush()  {  }
    default void close()  {  }
    default void open(opendoja.compat.sound.sampled.AudioFormat p0, int p1)  throws opendoja.compat.sound.sampled.LineUnavailableException {  }
    default void drain()  {  }
    default int write(byte[] p0, int p1, int p2)  { return 0; }
    default long getLongFramePosition()  { return 0L; }
}

