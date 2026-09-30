package opendoja.compat.sound.midi;

public interface Receiver extends java.lang.AutoCloseable {
    default void send(opendoja.compat.sound.midi.MidiMessage p0, long p1)  {  }
}

